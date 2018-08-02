function [kin_wrist, kin_elbow, kin_ori, kin_raw_time] = refineKinectData(kinData, rate, stidx)

% read data
kd = kinData.fulldata;
kd = kd(stidx:end);
N = length(kd);
kin_time = zeros(N, 1);
kin_wrist = zeros(N, 3);
kin_elbow = zeros(N, 3);
for i = 1:N
    kin_time(i) = kd(i).timestamp;
    kin_wrist(i, :) = kd(i).l_wrist;
    kin_elbow(i, :) = kd(i).l_elbow;
end

% match rate
kin_time = (kin_time - kin_time(1))/(10^7);
kin_raw_time = kin_time;
kin_wrist = resample(kin_wrist, kin_time, rate);
kin_elbow = resample(kin_elbow, kin_time, rate);

% get forarm direction
kin_ori = kin_wrist - kin_elbow;
for i = 1:size(kin_wrist, 1)
    kin_ori(i, :) = kin_ori(i, :)/norm(kin_ori(i, :));
end

end