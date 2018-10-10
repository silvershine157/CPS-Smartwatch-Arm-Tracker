function R = quatern2rotMat(q)
%QUATERN2ROTMAT Converts a quaternion orientation to a rotation matrix
%
%   R = quatern2rotMat(q)
%
%   Converts a quaternion orientation to a rotation matrix.
%
%   For more information see:
%   http://www.x-io.co.uk/node/8#quaternions
%
%	Date          Author                Notes
%	27/09/2011    SOH Madgwick          Initial release
%   22/01/2018    Philippe Lucidarme    Updated for [w x y z ]
%   
    
    qw = q(:,1);
    qx = q(:,2);
    qy = q(:,3);
    qz = q(:,4);
    

%     R(1,1,:) = 2.*qw.^2-1+2.*qx.^2;
%     R(1,2,:) = 2.*(qx.*qy+qw.*qz);
%     R(1,3,:) = 2.*(qx.*qz-qw.*qy);
%     R(2,1,:) = 2.*(qx.*qy-qw.*qz);
%     R(2,2,:) = 2.*qw.^2-1+2.*qy.^2;
%     R(2,3,:) = 2.*(qy.*qz+qw.*qx);
%     R(3,1,:) = 2.*(qx.*qz+qw.*qy);
%     R(3,2,:) = 2.*(qy.*qz-qw.*qx);
%     R(3,3,:) = 2.*qw.^2-1+2.*qz.^2;

    
    %% First row
    % 1 - 2*qy2 - 2*qz2
    R(1,1,:) = 1 - 2.*qy.^2 - 2.*qz.^2;
    % 2*qx*qy - 2*qz*qw
    R(1,2,:) = 2.*(qx.*qy - qz.*qw);
    %2*qx*qz + 2*qy*qw
    R(1,3,:) = 2.*(qx.*qz + qy.*qw);
    
    %% Second row
    % 2*qx*qy + 2*qz*qw
    R(2,1,:) = 2.*(qx.*qy + qz.*qw);
    % 1 - 2*qx2 - 2*qz2
    R(2,2,:) = 1 - 2.*qx.^2 - 2.*qz.^2;
    % 2*qy*qz - 2*qx*qw
    R(2,3,:) = 2.*(qy.*qz - qx.*qw);
    
    %% Third row
    % 2*qx*qz - 2*qy*qw
    R(3,1,:) = 2.*(qx.*qz - qy.*qw);
    % 2*qy*qz + 2*qx*qw
    R(3,2,:) = 2.*(qy.*qz + qx.*qw);
    % 1 - 2*qx2 - 2*qy2
    R(3,3,:) = 1 - 2.*qx.^2 - 2.*qy.^2;
end

