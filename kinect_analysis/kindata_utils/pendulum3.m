clear
gydat = load('gypos_7');
gypos = gydat.P;
gytime = gydat.time;
rate = 100;
gypos = resample(gypos, gytime, rate);
gypos = gypos(1:end - 50, :);

[~, Li1] = max(gypos(:, 1));
[~, Ri1] = min(gypos(:, 1));
[~, Li2] = min(gypos(:, 3));
[~, Ri2] = max(gypos(:, 3));
Li = floor((Li1 + Li2)/2);
Ri = floor((Ri1 + Ri2)/2);
gyscale = 1/1.5;
gypos = gyscale * gypos;
gypos = gypos(1:Ri, :);

kdat = load('saved data/prof putting/7/kdat');
mPos = resample(kdat.kd.mp, kdat.kd.ts, rate);
ssPos = resample(kdat.kd.ss, kdat.kd.ts, rate);

mPos = mPos(100:end-100, :);
ssPos = ssPos(100:end-50, :);

gylen = zeros(size(gypos, 1), 1);
for k = 1:size(gypos, 1)
    gylen(k) = norm(gypos(k, :) - gypos(1, :));
end

kstidx = 1;
kinlen = zeros(size(mPos, 1), 1);
for k = 1:size(mPos, 1)
   kinlen(k) = norm(mPos(kstidx+k-1, :) - mPos(kstidx, :));
end

kstidx = scalarTimeSync(kinlen, gylen);
N = size(gypos, 1);
ssPos = ssPos(kstidx:kstidx+N-1, :);
mPos = mPos(kstidx:kstidx+N-1,:);
mRelPos = mPos - mean(ssPos, 1);
mDir = zeros(size(mRelPos));
gydir = mDir;
for i = 1:N
   mDir(i, :) = mRelPos(i, :)/norm(mRelPos(i, :));
end
for i = 1:N
    gyDir(i, :) = gypos(i, :)/norm(gypos(i, :));
end
K = fitRotationMatrix(mDir, gyDir);

Kgypos = zeros(size(mRelPos));
for i = 1:N
    Kgypos(i, :) = K*(gypos(i, :)');
end

err = zeros(size(mRelPos, 1), 1);
for i = 1:N
    err(i) = norm(mRelPos(i, :) - Kgypos(i, :));
end
meanerr = mean(err);

plot(mRelPos, 'r');
hold on
plot(Kgypos, 'b');
hold off