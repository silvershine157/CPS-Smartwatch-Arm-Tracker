function [wPos] = refinePosData(wPosData, rate, stidx)
time = wPosData.time(stidx:end);
pos = wPosData.pos(stidx:end, :);
wPos = resample(pos, time, rate);
end

