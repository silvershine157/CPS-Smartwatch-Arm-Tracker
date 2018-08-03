function [R] = quatToMat(quat)
% ensure quaternion is normalized
quat = quat/norm(quat);
x = quat(1);
y = quat(2);
z = quat(3);
w = quat(4);
R = [(1-2*(y*y+z*z)) 2*(x*y-w*z) 2*(w*y+x*z); 2*(x*y+w*z) (1-2*(x*x+z*z)) 2*(y*z-w*x); 2*(x*z-w*y) 2*(w*x+y*z) (1-2*(x*x+y*y))];
end

