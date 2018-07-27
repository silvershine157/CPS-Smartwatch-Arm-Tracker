clear;

%% PARAMETERS
PATH = '../data/';
TAG = '';
AXIS = 3;
INIT_RANGE = 1:10;
TARGET_RANGE = 80 + (1:600);

%% LOAD DATA
accel = load([PATH, TAG, '_globalAccel.txt']);
% accel = accel(TARGET_RANGE, :);

time = (accel(:, 1) - accel(1, 1)) / 10^9; % sensing time relative to the sensing start time (unit: second)
accel = accel(:, 2:4);

% %% SIMPLE SIGNAL PROCESSING
% accel = accel - mean(accel(INIT_RANGE)); % gravity removal

RATE = 100;
FREQ = 5;
[b, a] = butter(4, 2 * FREQ / RATE, 'low');
% for cnt = 1:3
%     accel(:, cnt) = filter(b, a, accel(:, cnt));
% end


%% VELOCITY & POSITION ESTIMATION
vel = zeros(size(accel));
pos = zeros(size(accel));

for cnt = 2:size(accel, 1)
    deltaT = time(cnt) - time(cnt - 1);
%     deltaT = 0.01;
    for i = 1:3        
        vel(cnt,i) = vel(cnt - 1,i) + accel(cnt,i) * deltaT;
        pos(cnt,i) = pos(cnt - 1,i) + vel(cnt - 1,i) * deltaT + 1/2 * accel(cnt,i) * deltaT^2;
    end
end

distance = pos(2,1) - pos(cnt,1);
disp(distance) 


%% PLOT
subplot 331
plot(pos(:,1))

subplot 332
plot(vel(:, 1))

subplot 333
plot(accel(:, 1))

subplot 312
plot(pos(:,2))

subplot 313
% plot3(pos(:,1), pos(:,2), pos(:,3), '.')
plot(pos(:, 1), pos(:, 2));


% grid on
% xlabel('x-axis')
% ylabel('y-axis')
% zlabel('z-axis')