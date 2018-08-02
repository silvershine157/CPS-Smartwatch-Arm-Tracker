clear;

%% PARAMETERS
PATH = '';
TAG = '';
AXIS = 3;
% TARGET_RANGE = (170:700);

%% LOAD DATA
accel = load([PATH, TAG, '_globalAccel_sand3.txt']);
% accel = accel(TARGET_RANGE, :);

time = (accel(:, 1) - accel(1, 1)) / 10^9; % sensing time relative to the sensing start time (unit: second)
accel = accel(:, 2:4);

disp(time);

RATE = 100;
FREQ = 5;
[b, a] = butter(2, FREQ / RATE, 'low');
[d, c] = butter(4, 0.1 * FREQ / RATE, 'high');
for cnt = 1:3
    accel(:, cnt) = filter(b, a, accel(:, cnt));
end

%% VELOCITY & POSITION ESTIMATION
vel = zeros(size(accel));
pos = zeros(size(accel));

rww = mean(abs(accel))
for cnt = 1:3
    vel(:, cnt) = cumtrapz(time,accel(:, cnt));
end

rw = mean(abs(vel))
subplot 221
plot(vel(:,2));

for cnt = 1:3
    vel(:, cnt) = filter(d, c, vel(:, cnt));
end

rw = mean(abs(vel))
subplot 222
plot(vel(:,2));
for cnt = 1:3
    pos(:, cnt) = cumtrapz(time, vel(:, cnt));
end


%% PLOT
subplot 223
plot(pos(:, 1), pos(:, 2));

subplot 224
plot3(pos(:,1), pos(:,2), pos(:,3), '.')

grid on
xlabel('x-axis')
ylabel('y-axis')
zlabel('z-axis')

save('posData_sand3.mat', 'pos', 'time')
clear