clear
rawdat = load('kindata.txt');
kd = kinData;
for i = 1:size(rawdat, 1)
    kd.ts(i, 1) = rawdat(i, 1);
    
    if(rawdat(i, 2) > 0)
        kd.haveMarker(i, 1) = true;
    else
        kd.haveMarker(i, 1) = false;
    end
    kd.mp(i, :) = rawdat(i, 3:5);
    
    if(rawdat(i, 6) > 0)
        kd.haveBody(i, 1) = true;
    else
        kd.haveBody(i, 1) = false;
    end
    
    kd.ls(i, :) = rawdat(i, 7:9);
    kd.le(i, :) = rawdat(i, 10:12);
    kd.lw(i, :) = rawdat(i, 13:15);
end
save('kdat.mat', 'kd');
clear i kd