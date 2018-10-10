function in = quaternInv(a)
%QUATERNPROD Calculates the quaternion product
%
%   in = quaternInv(a)
%
%   Calculates the quaternion inverse of quaternion a.
%

    denom = sum(a.^2);
    in(:,1) = a(:,1)./denom;
    in(:,2) = -a(:,2)./denom;
    in(:,3) = -a(:,3)./denom;
    in(:,4) = -a(:,4)./denom;
end

