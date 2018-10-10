% Test script for synchronizing Kinect and Smartwatch data

rotVecFilename = 'rotVec_random.txt';
kinectFilename = 'recordedDataRandom.mat';
posDatFilename = 'posData_sand3.mat';

kinData = load(kinectFilename);
wRotData = load(rotVecFilename);
wPosData = load(posDatFilename);

[K, k_stidx, w_stidx] = syncData(kinData, wRotData);
kinectCompare(kinData, wPosData, K, k_stidx, w_stidx);
