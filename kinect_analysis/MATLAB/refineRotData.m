function [Rarrays] = refineRotData(wRotData, rate)
% R : n x 3 x 3 rotation matrices
time = wRotData(:, 1);
time = (time - time(1))/(10^9);
quats = wRotData(:, 2:5);
quats = resample(quats, time, rate);
N = size(quats, 1);
Rarrays = zeros([N, 3, 3]);
for i = 1:N
   Rarrays(i, :, :) = quatToMat(quats(i, :));
end

end

