close all;
clear all;
clc;


%% Initial parameters
PATH = '/home/cps-sunjae/Desktop/CPS-sensor/data/amateur/';
% PATH = '/home/cps-sunjae/Desktop/CPS-sensor/data/gyro/';
TAG = '';
BIAS_RANGE = 1:20;
START_SAMPLE = 21;

%% LOAD GYRO DATA
gyro = load([PATH, TAG, 'tilt_coord3.txt']);

gyro = gyro(START_SAMPLE:end,:);

abs_gyro_time = gyro(:,1);

time = (gyro(:, 1) - gyro(1, 1)) / 10^3; % sensing time relative to the sensing start time (unit: second)
gyro = gyro(:, 2:4);

% Get radian
rad = zeros(size(gyro,1),4);
for cnt = 1:3
    rad(:, cnt) = cumtrapz(time,gyro(:, cnt));
end

%% Peak Filter
figure(1)
subplot 221
plot(rad)
title('raw y-axis radian')

% find peak of y-axis radian (start of down swing)
% l = location of peak.
% w = width of peak
[p, l, w, p] = findpeaks(rad(:,2), 'MinPeakDistance', size(rad,1)/2, 'MinPeakHeight', 0.0, 'Annotate', 'extents', 'WidthReference', 'halfprom');
%find peak of negate of y-axis radian (end of down swing)
[p1, l1, w1, p1] = findpeaks(rad(:,2).*-1, 'MinPeakDistance', size(rad,1)/2, 'MinPeakHeight', 0.0, 'Annotate', 'extents', 'WidthReference', 'halfprom');

% 1st extracion. re-define range using l and w
START_POS = l(1)-2*w(1);
END_POS = l(1)+3*w(1);

START_POS = max([START_POS 1]);
END_POS = min([END_POS size(time,1)]);

GYRO_OFFSET = round(START_POS);

disp 'start pos'
disp (START_POS)
disp 'end pos'
disp (END_POS)

gyro = gyro(START_POS:END_POS,:);
time = time(START_POS:END_POS,:);
time = (time(:,1) - time(1,1));

% get radian again with new range
rad = zeros(size(gyro,1),4);
for cnt = 1:3
    rad(:, cnt) = cumtrapz(time,gyro(:, cnt));
end

subplot 222
plot(rad(:,2));
title('1st extraction of radian')

% down swing time
SWING_TIME = l1(1) - l(1);
disp 'swing time'
disp (SWING_TIME)

% find peak of angular accelartion ( start of back swing)
[p2, l2, w2, p2] = findpeaks(diff(gyro(1:l(1)-START_POS,2)), 'SortStr','descend','Annotate', 'extents', 'WidthReference', 'halfprom');
PRE_START_POS = max([l2(1)-10 1]);

while rad(PRE_START_POS,2) > 0.01
    PRE_START_POS = PRE_START_POS - 10;
end

START_POS = PRE_START_POS;

disp 'new start'
disp (START_POS)
% subtract bias
% for cnt = 1:3
%     gyro(:,cnt) = gyro(:,cnt) - mean(gyro(1:START_POS,cnt));
% end

% 2nd extraction. redefine range.
GYRO_OFFSET = GYRO_OFFSET + round(START_POS);
gyro = gyro(START_POS:end,:);
time = time(START_POS:end,:);
time = (time(:,1) - time(1,1));

% % resample to 100 hz
% gyro = resample(gyro,time,75);
% time_resample = zeros(size(gyro,1),1);
% for cnt = 2:size(gyro,1)
%     time_resample(cnt) = time_resample(cnt-1) + 0.01;
% end

% get radian again with new range
rad = zeros(size(gyro,1),4);
for cnt = 1:3
    rad(:, cnt) = cumtrapz(time,gyro(:, cnt));
end

subplot 223
plot(rad(:,2));
title('2nd extraction of radian')

subplot 224
stem(time, gyro);
legend('x','y','z');
title('2nd extraction of gyro')

% RATE = 100;
% FREQ = 1;
% [d, c] = butter(1, 0.1*FREQ/RATE, 'high');
% figure(5)
% freqz(d,c);
% 
% for cnt = 1:3
%     gyro(:, cnt) = filter(d, c, gyro(:, cnt));
% end


%% Adjust Coordinates
PATH_ROT_WATCH = '/home/cps-sunjae/Desktop/CPS-sensor/data/amateur/rot/watch/';
rot_vec_watch = load([PATH_ROT_WATCH, TAG, 'tilt_coord3.txt']);

PATH_ROT_PHONE = '/home/cps-sunjae/Desktop/CPS-sensor/data/amateur/rot/phone/';
rot_vec_phone = load([PATH_ROT_PHONE, TAG, 'tilt_coord3.txt']);

% find rotation vector of the time when swing starts.
abs_gyro_start = abs_gyro_time(GYRO_OFFSET);
[diff, index_watch] = min(abs(abs_gyro_start - rot_vec_watch(:,1)));
[diff, index_phone] = min(abs(abs_gyro_start - rot_vec_phone(:,1)));

% change android quaternion to matlab quaternion format.
local_rotVec_watch(1) = rot_vec_watch(index_watch,5);
local_rotVec_watch(2) = rot_vec_watch(index_watch,2);
local_rotVec_watch(3) = rot_vec_watch(index_watch,3);
local_rotVec_watch(4) = rot_vec_watch(index_watch,4);

% local_rotVec_phone(1) = mean(rot_vec_phone(:,5));
% local_rotVec_phone(2) = mean(rot_vec_phone(:,2));
% local_rotVec_phone(3) = mean(rot_vec_phone(:,3));
% local_rotVec_phone(4) = mean(rot_vec_phone(:,4));
local_rotVec_phone(1) = (rot_vec_phone(index_phone,5));
local_rotVec_phone(2) = (rot_vec_phone(index_phone,2));
local_rotVec_phone(3) = (rot_vec_phone(index_phone,3));
local_rotVec_phone(4) = (rot_vec_phone(index_phone,4));

disp 'phone vector'
disp(local_rotVec_phone);

G_phone = quaternProd(quaternProd(quaternInv(local_rotVec_phone), [0 0 1 0]),local_rotVec_phone);
G_xyPhone = [G_phone(2:3)/norm(G_phone(2:3)) 0];
cosTheta = dot(G_xyPhone, [0 1 0]);
Theta = acos(cosTheta);
%0.43879 0.7378 -0.5137 122 degree

disp 'tetha'
disp(Theta);

ZrotMat = [cos(Theta) -sin(Theta) 0; sin(Theta) cos(Theta) 0; 0 0 1];

% local coordinate of club head
W = quaternProd(quaternProd(local_rotVec_watch,[0 0 0 -1.4]), quaternInv(local_rotVec_watch));
disp(W);

%% Template for quaternion
quat = zeros(size(gyro,1),4);
for cnt = 1:3
    quat(:,cnt+1) = gyro(:,cnt);
end

%% Initial quaternion
Q(1,:)=[1 0 0 0];

%% Initial coordinates of the point
P(1,:) = [W(2),W(3),W(4)];
PX(1,:)=[2,0,0];
PY(1,:)=[1,1,0];
PZ(1,:)=[1,0,1];


%% Main loop
for i=2:size(gyro(:,1))
    
    %% Simulate gyroscope value 

    %% Update orientation
    % Compute the quaternion derivative
    Qdot=quaternProd(0.5*Q(i-1,:),quat(i,:));
    
    % Update the estimated position
    Q(i,:)=Q(i-1,:)+Qdot*(time(i)-time(i-1));
    
    % Normalize quaternion
    Q(i,:)=Q(i,:)/norm(Q(i,:));
    
    %% Update point coordinates
    % Compute the associated transformation marix
    M=quatern2rotMat(Q(i,:));
    
    P(i,:)=(M*P(1,:)')';
    PX(i,:)=(M*PX(1,:)')';
    PY(i,:)=(M*PY(1,:)')';
    PZ(i,:)=(M*PZ(1,:)')';
    
    % Display the new point
%     plot3 (P(i,1),P(i,2),P(i,3),'k.');
%     plot3 (PX(i,1),PX(i,2),PX(i,3),'r.');
%     plot3 (PY(i,1),PY(i,2),PY(i,3),'g.');
%     plot3 (PZ(i,1),PZ(i,2),PZ(i,3),'b.');
%     line ( [ P(i,1) , PX(i,1) ] , [ P(i,2) , PX(i,2) ],  [ P(i,3) , PX(i,3) ] ,'Color','r');
%     line ( [ P(i,1) , PY(i,1) ] , [ P(i,2) , PY(i,2) ],  [ P(i,3) , PY(i,3) ] ,'Color','g');
%     line ( [ P(i,1) , PZ(i,1) ] , [ P(i,2) , PZ(i,2) ],  [ P(i,3) , PZ(i,3) ] ,'Color','b');

%     figure(2)
%     plot3 (P(i,1),P(i,2),P(i,3),'k.');
%     hold on;
%     axis square equal;
%     grid on;
% 
%     xlabel ('x');
%     ylabel ('y');
%     zlabel ('z');
%     drawnow;    
end

% %% replace 1 0 1 with rotated 0 0 1.4
% figure(3)
% % plot3 ([0 G(2)], [0 G(3)], [0 G(4)], '.r');
% plot3 ([0 0], [0 0], [0 -1.4], 'r');
% hold on;
% grid on;
% xlabel('x');
% ylabel('y');
% zlabel('z');
% plot3 ([0 W(2)], [0 W(3)], [0 W(4)], 'b');

% local 3D points to quaternion format
L = zeros(size(gyro,1),4);
for cnt = 1:3
    L(:,cnt+1) = P(:,cnt);
end

facing_P = zeros(size(P,1),3);
flipQuatern = [cos(pi/2) 0 sin(pi/2) 0];
for i=1:size(P,1)
    % Local quaternion points to global quaternion points
    G(i,:) = quaternProd(quaternProd(quaternInv(local_rotVec_watch), L(i,:)),local_rotVec_watch);
%     G(i,:) = quaternProd(quaternProd(flipQuatern, G(i,:)), quaternInv(flipQuatern));
%     figure(2)
%     plot3 (G(i,2),G(i,3),G(i,4),'k.');
%     hold on;
%     axis square equal;
%     grid on;
%     xlabel ('x');
%     ylabel ('y');
%     zlabel ('z');
    
    % Global quaternion points to Face-Direction quaternion points
%     F(i,:) = quaternProd(quaternProd(xyReflection_quatern, G(i,:)), quaternInv(xyReflection_quatern));
%     for j=1:3
%         facing_P(i,j) = F(i,j+1);
%     end
    
    facing_P(i,:) = transpose(ZrotMat*(transpose(G(i,2:end))));
%     
    figure(3)
    plot3 (facing_P(i,1),facing_P(i,2),facing_P(i,3),'k.');
    hold on;
    axis square equal;
    grid on;
    xlabel ('x');
    ylabel ('y');
    zlabel ('z');
end







% 
% [p3, l3, w3, p3] = findpeaks(rad(:,2), 'MinPeakDistance', size(rad,1)/2,'Annotate', 'extents', 'WidthReference', 'halfprom');
% [p4, l4, w4, p4] = findpeaks(rad(:,2).*-1, 'MinPeakDistance', size(rad,1)/2,'Annotate', 'extents', 'WidthReference', 'halfprom');
% 
% P(:, :) = P(:, :) - [1 0 1];
% origin_points = [];
% origin_index = 0;
% min_dist = 1;
% for i=l3(1):l4(1)
%     temp = P(i,:);
%     distrib = abs(P(i,1)) + abs(P(i,3));
%     if distrib < min_dist
%         origin_points = P(i,:);
%         min_dist = distrib;
%         origin_index = i;
%     end
% end
% 
% 
% % subtract bias
% for cnt = 1:3
%     gyro(:,cnt) = gyro(:,cnt) - ((P(origin_index,cnt)/origin_index)/0.01);
% end
% 
% disp(P(origin_index,:))
% disp(origin_index)
% disp (((P(origin_index,:)/origin_index)/0.01))
% %% Template for quaternion
% quat = zeros(size(gyro,1),4);
% for cnt = 1:3
%     quat(:,cnt+1) = gyro(:,cnt);
% end
% 
% %% Initial quaternion
% Q(1,:)=[1 0 0 0];
% 
% %% Initial coordinates of the point
% P(1,:) = [1,0,1];
% PX(1,:)=[2,0,0];
% PY(1,:)=[1,1,0];
% PZ(1,:)=[1,0,1];
% 
% 
% %% Main loop
% for i=2:size(gyro(:,1))
%     
%     %% Simulate gyroscope value 
% 
%     %% Update orientation
%     % Compute the quaternion derivative
%     Qdot=quaternProd(0.5*Q(i-1,:),quat(i,:));
%     
%     % Update the estimated position
%     Q(i,:)=Q(i-1,:)+Qdot*(time_resample(i)-time_resample(i-1));
%     
%     % Normalize quaternion
%     Q(i,:)=Q(i,:)/norm(Q(i,:));
%     
%     %% Update point coordinates
%     % Compute the associated transformation marix
%     M=quatern2rotMat(Q(i,:));
%     
%     % Calculate the coordinate of the new point
% %     P(i,:) = quaternProd(quaternProd(Q(i,:),P(1,:)), quaternInv(Q(i,:)));
%     P(i,:)=(M*P(1,:)')';
%     PX(i,:)=(M*PX(1,:)')';
%     PY(i,:)=(M*PY(1,:)')';
%     PZ(i,:)=(M*PZ(1,:)')';
%     
%     % Display the new point
% %     plot3 (P(i,1),P(i,2),P(i,3),'k.');
% %     plot3 (PX(i,1),PX(i,2),PX(i,3),'r.');
% %     plot3 (PY(i,1),PY(i,2),PY(i,3),'g.');
% %     plot3 (PZ(i,1),PZ(i,2),PZ(i,3),'b.');
% %     line ( [ P(i,1) , PX(i,1) ] , [ P(i,2) , PX(i,2) ],  [ P(i,3) , PX(i,3) ] ,'Color','r');
% %     line ( [ P(i,1) , PY(i,1) ] , [ P(i,2) , PY(i,2) ],  [ P(i,3) , PY(i,3) ] ,'Color','g');
% %     line ( [ P(i,1) , PZ(i,1) ] , [ P(i,2) , PZ(i,2) ],  [ P(i,3) , PZ(i,3) ] ,'Color','b');
% 
%     figure(3)
%     plot3 (P(i,1),P(i,2),P(i,3),'k.');
%     hold on;
%     axis square equal;
%     grid on;
% 
%     xlabel ('x');
%     ylabel ('y');
%     zlabel ('z');
%     drawnow;    
% end
