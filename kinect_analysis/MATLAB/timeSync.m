function sample_ofs = timeSync(kOri, wOri)
% estimate how much sample offset is needed to sync kinect and watch data

[fb, fa] = butter(2, 0.01, 'low');
for axis = 1:3
   kOri(:, axis) = filter(fb, fa, kOri(:, axis));
   wOri(:, axis) = filter(fb, fa, wOri(:, axis));
end

kDiff = kOri(2:end, :) - kOri(1:end-1, :);
wDiff = wOri(2:end, :) - wOri(1:end-1, :);

kPower = zeros(size(kDiff, 1), 1);
for i=1:size(kDiff, 1)
    kPower(i) = norm(kDiff(i, :));
end
wPower = zeros(size(wDiff, 1), 1);
for i=1:size(wDiff, 1)
    wPower(i) = norm(wDiff(i, :));
end

% rough search
intv = 10;
searchPoints = (2*intv):intv:(length(kPower) - length(wPower) - intv);
errorPoints = 1:intv:(length(wPower) - intv);

bestError = length(errorPoints)*(max(max(wPower),max(wPower))^2);
bestOfs = 0;
for ofs = searchPoints
    currentError = 0;
    for i = errorPoints
        currentError = currentError + (wPower(i) - kPower(ofs + i))^2;
    end
    if(currentError < bestError)
       bestError = currentError;
       bestOfs = ofs;
    end
end

% detailed search
searchPoints = (bestOfs-intv):(bestOfs+intv);
errorPoints = 1:2:(length(wPower) - intv);
bestError = length(errorPoints)*(max(max(wPower),max(wPower))^2);
bestOfs = 0;
for ofs = searchPoints
    currentError = 0;
    for i = errorPoints
        currentError = currentError + (wPower(i) - kPower(ofs + i))^2;
    end
    if(currentError < bestError)
       bestError = currentError;
       bestOfs = ofs;
    end
end

plot(kPower(bestOfs:end))
hold on
plot(wPower)
hold off

sample_ofs = bestOfs;
end

