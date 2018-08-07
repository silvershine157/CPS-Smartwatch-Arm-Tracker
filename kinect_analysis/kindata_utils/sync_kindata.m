function [K, k_stidx, w_stidx] = sync_kindata(kd, wRotData)
% OUTPUT
% K : 3x3 rot mat converts smartwatch-global to kinect camera space
% k_stidx : start index of kinect data
% k_stidx : start index of watch data

w_stidx = 50;

% refine data

common_rate = 100;

kRawTime = kd.ts;
kOri = kd.lw - kd.le;
kOri = resample(kOri, kd.ts, common_rate);
for i=1:size(kOri, 1)
   kOri(i, :) = kOri(i, :)/norm(kOri(i, :)); 
end

[wR] = refineRotData(wRotData(w_stidx:end,:), common_rate);

% get arm direction in w-global coordinates
local_arm = [1 0 0]';
wOri = zeros(size(wR, 1), 3);
for i = 1:size(wR, 1)
    wRi = squeeze(wR(i, :, :));
    wOri(i, :) = wRi*local_arm;
end

% match time sequence
sample_ofs = timeSync(kOri, wOri);
kOri = kOri(sample_ofs:sample_ofs + size(wOri, 1) - 1, :);

K = fitRotationMatrix(kOri, wOri);

KwOri = zeros(size(wOri));
for i = 1:size(wOri, 1)
    KwOri(i, :) = K * (wOri(i, :)');
end

% find kinect start index
T = sample_ofs/common_rate;
temp = find(kRawTime > T);
k_stidx = temp(1);

subplot 311
plot(kOri(:, 1))
hold on
plot(KwOri(:, 1))
hold off

subplot 312
plot(kOri(:, 2))
hold on
plot(KwOri(:, 2))
hold off

subplot 313
plot(kOri(:, 3))
hold on
plot(KwOri(:, 3))
hold off

end