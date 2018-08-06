clear
kdat = load('kdat');
space_range = [-1 1 1 2 -1 1];
sample_range = 300:600;
rate = 50;
mPos = resample(kdat.kd.mp, kdat.kd.ts, rate);
ePos = resample(kdat.kd.le, kdat.kd.ts, rate);
wPos = resample(kdat.kd.lw, kdat.kd.ts, rate);
sPos = resample(kdat.kd.ls, kdat.kd.ts, rate);

mPos = mPos(sample_range, :);
ePos = ePos(sample_range, :);
wPos = wPos(sample_range, :);
sPos = sPos(sample_range, :);

hold off;
plot3(mPos(:, 1), mPos(:, 3), mPos(:, 2), '*b');
hold on;
plot3(wPos(:, 1), wPos(:, 3), wPos(:, 2), '*r');
plot3(ePos(:, 1), ePos(:, 3), ePos(:, 2), '*g');
plot3(sPos(:, 1), sPos(:, 3), sPos(:, 2), '*y');

%{
% Animation
skip = 10;
hold off;
figure
hold on;
for i = 1:skip:size(mPos, 1)
    
    plot3(mPos(i, 1), mPos(i, 3), mPos(i, 2), '*b');
    plot3(wPos(i, 1), wPos(i, 3), wPos(i, 2), '*r');
    plot3(ePos(i, 1), ePos(i, 3), ePos(i, 2), '*g');
    plot3(sPos(i, 1), sPos(i, 3), sPos(i, 2), '*y');
    
    axis(space_range);
    drawnow;
    pause(1/rate);
end
%}
