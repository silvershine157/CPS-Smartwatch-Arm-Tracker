close all;
clear all;

%% Initial parameters
PATH = '/home/cps-sunjae/Desktop/CPS-sensor/data/phone/belly/';
% PATH = '/home/cps-sunjae/Desktop/CPS-sensor/data/gyro/';
TAG = '';

%% LOAD DATA
gyro = load([PATH, TAG, 'gyro1.txt']);
accel = load([PATH, TAG, 'accel1.txt']);

figure(1)
subplot 221
plot(gyro)
title("gyro")
legend('x','y','z')
hold on 
subplot 222
plot(accel)
title("accel")
legend('x','y','z')

gyro2 = load([PATH, TAG, 'gyro2.txt']);
accel2 = load([PATH, TAG, 'accel2.txt']);

subplot 223
plot(gyro2)
title("gyro2")
legend('x','y','z')

subplot 224
plot(accel2)
title("accel2")
legend('x','y','z')

rot1 = load([PATH, TAG, 'rot1.txt']);
rot2 = load([PATH, TAG, 'rot2.txt']);
rot1 = rot1(:, 2:4);
rot2 = rot2(:, 2:4);
figure(2)
subplot 221
plot(rot1)
title("rot1")
legend('x','y','z')
subplot 222
plot(rot2)
legend('x','y','z')
title("rot2")
