function kinectCompare(kinData, wPosData, K, k_stidx, w_stidx)

% refine data
common_rate = 30;
wPos = refinePosData(wPosData, common_rate, w_stidx);
[kWrist, ~, ~, ~] = refineKinectData(kinData, common_rate, k_stidx);
N = size(wPos, 1);
kWrist = kWrist(1:N, :);

% rotate watch position
KwPos = zeros(size(wPos));
for i = 1:N
    KwPos(i, :) = K*wPos(i, :)';
end


% align initial positions to origin
kWrist = kWrist - kWrist(1,:);
KwPos = KwPos - KwPos(1,:);

% lets take a look
subplot 231
plot3(wPos(:,1), wPos(:,2), wPos(:,3), 'r')
xlabel('x');
ylabel('y');
zlabel('z');
axis([-1 1 -1 1 -1 1])

subplot 232
plot3(kWrist(:,1), kWrist(:,2), kWrist(:,3), 'b')
hold on
plot3(KwPos(:,1), KwPos(:,2), KwPos(:,3), 'r')
hold off
xlabel('x');
ylabel('y');
zlabel('z');
axis([-1 1 -1 1 -1 1])

end
