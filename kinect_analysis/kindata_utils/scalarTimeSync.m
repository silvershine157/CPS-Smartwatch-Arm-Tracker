function sample_ofs = scalarTimeSync(kdat,wdat)

kPower = kdat;
wPower = wdat;

% rough search
intv = 10;
searchPoints = (2*intv):intv:(length(kPower) - length(wPower) - intv);
errorPoints = 1:intv:(length(wPower) - intv);

bestError = length(errorPoints)*(max(max(wPower),max(wPower))^2);
bestOfs = 0;
for ofs = searchPoints
    currentError = 0;
    for i = errorPoints
        currentError = currentError + (wPower(i) - kPower(ofs + i))^2;
    end
    if(currentError < bestError)
       bestError = currentError;
       bestOfs = ofs;
    end
end

% detailed search
searchPoints = (bestOfs-intv):(bestOfs+intv);
errorPoints = 1:2:(length(wPower) - intv);
bestError = length(errorPoints)*(max(max(wPower),max(wPower))^2);
bestOfs = 0;
for ofs = searchPoints
    currentError = 0;
    for i = errorPoints
        currentError = currentError + (wPower(i) - kPower(ofs + i))^2;
    end
    if(currentError < bestError)
       bestError = currentError;
       bestOfs = ofs;
    end
end

sample_ofs = bestOfs;

end

