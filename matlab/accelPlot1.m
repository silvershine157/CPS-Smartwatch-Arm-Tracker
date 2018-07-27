clear;

%% PARAMETERS
PATH = '../../data/';
TAG = '';
AXIS = 3;

%% LOAD DATA
accel = load([PATH, TAG, '_globalAccel.txt']);

time = (accel(:, 1) - accel(1, 1)) / 10^9; % sensing time relative to the sensing start time (unit: second)
accel = accel(:, 2:4);

RATE = 100;
FREQ = 5;
[b, a] = butter(4, FREQ / RATE, 'low');
[d, c] = butter(1, FREQ / RATE, 'high');

for cnt = 1:3
    accel(:, cnt) = filter(b, a, accel(:, cnt));
end

%% VELOCITY & POSITION ESTIMATION
vel = zeros(size(accel));
pos = zeros(size(accel));

for cnt = 1:3
    vel(:, cnt) = cumtrapz(time,accel(:, cnt));
end

for cnt = 1:3
    vel(:, cnt) = filter(d, c, vel(:, cnt));
end

for cnt = 1:3
    pos(:, cnt) = cumtrapz(time, vel(:, cnt));
end



%% PLOT
subplot 331
plot(accel(:,1))

subplot 332
plot(vel(:, 1))

subplot 333
plot(pos(:, 1))

subplot 313
%plot3(pos(:,1), pos(:,2), pos(:,3), '.')
plot(pos(:, 1), pos(:, 2));

% grid on
xlabel('x-axis')
ylabel('y-axis')
zlabel('z-axis')