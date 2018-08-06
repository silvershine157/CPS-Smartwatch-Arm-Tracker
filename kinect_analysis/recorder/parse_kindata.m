rawdat = load('kindata.txt');
kindata = kinDataTuple;
for i = 1:size(rawdat, 1)
    kdt = kinDataTuple;
    kdt.ts = rawdat(i, 1);
    if(rawdat(i, 2) > 0)
        kdt.haveMarker = true;
    else
        kdt.haveMarker = false;
    end
    kdt.mp = rawdat(i, 3:5);

    if(rawdat(i, 6) > 0)
        kdt.haveBody = true;
    else
        kdt.haveBody = false;
    end
    kdt.ls = rawdat(i, 7:9);
    kdt.le = rawdat(i, 10:12);
    kdt.lw = rawdat(i, 13:15);
    kindata(i, 1) = kdt;
    save kindata
end
