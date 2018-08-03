// kinectTracker3.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"

#include <stdio.h>

#include <sstream>
#include <string>
#include <iostream>
#include <opencv2/opencv.hpp>
#include <opencv2\highgui\highgui.hpp>

#include <Windows.h>
#include <Ole2.h>
#include <gl/GL.h>
#include <gl/GLU.h>
#include <gl/glut.h>

#include <Kinect.h>

using namespace std;
using namespace cv;


// window names
const string checkWindowName = "Check Window";
const string controlWindowName = "Control Window";

// Tracking variables
int H_MIN, H_MAX, S_MIN, S_MAX, V_MIN, V_MAX;
bool recording;
bool stopped;

// body tracking
BOOLEAN tracked;
Joint joints[JointType_Count];

// Kinect constants
const int d_width = 512;
const int d_height = 424;
const int c_width = 1920;
const int c_height = 1080;

// Kinect buffers
unsigned char rgbimage[c_width*c_height * 4];
CameraSpacePoint rgb2xyz[c_width*c_height];

// Kinect variables
IKinectSensor* sensor;
IMultiSourceFrameReader* reader;
ICoordinateMapper* mapper;

// Control panel
Mat3b ctrl_canvas;
Rect record_button;


void loadHSVRange(int * hm, int * hM, int * sm, int * sM, int * vm, int * vM) {
	// currently hard-coded
	*hm = 83;
	*hM = 100;
	*sm = 102;
	*sM = 202;
	*vm = 16;
	*vM = 192;
}

bool initKinect() {
	if (FAILED(GetDefaultKinectSensor(&sensor))) {
		return false;
	}
	if (sensor) {
		sensor->get_CoordinateMapper(&mapper);
		sensor->Open();
		sensor->OpenMultiSourceFrameReader(
			FrameSourceTypes::FrameSourceTypes_Depth 
			| FrameSourceTypes::FrameSourceTypes_Color
			| FrameSourceTypes::FrameSourceTypes_Body,
			&reader);
		return reader;
	}
	else {
		return false;
	}
}


void getDepthData(IMultiSourceFrame* frame) {
	IDepthFrame* depthframe;
	IDepthFrameReference* frameref = NULL;
	frame->get_DepthFrameReference(&frameref);
	frameref->AcquireFrame(&depthframe);
	if (frameref) frameref->Release();
	if (!depthframe) return;
	// Get data from frame
	unsigned int sz;
	unsigned short* buf;
	depthframe->AccessUnderlyingBuffer(&sz, &buf);
	mapper->MapColorFrameToCameraSpace(d_width*d_height, buf, c_width*c_height, rgb2xyz);
	if (depthframe) depthframe->Release();
}


void getRgbData(IMultiSourceFrame* frame) {
	IColorFrame* colorframe;
	IColorFrameReference* frameref = NULL;
	frame->get_ColorFrameReference(&frameref);
	frameref->AcquireFrame(&colorframe);
	if (frameref) frameref->Release();
	if (!colorframe) return;
	// Get data from frame
	colorframe->CopyConvertedFrameDataToArray(c_width*c_height * 4, rgbimage, ColorImageFormat_Bgra);
	if (colorframe) colorframe->Release();
}

void getBodyData(IMultiSourceFrame* frame) {
	IBodyFrame* bodyframe;
	IBodyFrameReference* frameref = NULL;
	DetectionResult dr;
	frame->get_BodyFrameReference(&frameref);
	frameref->AcquireFrame(&bodyframe);
	if (frameref) frameref->Release();

	if (!bodyframe) return;

	IBody* body[6] = { 0 };
	bodyframe->GetAndRefreshBodyData(6, body);
	for (int i = 0; i < 6; i++) {
		body[i]->get_IsTracked(&tracked);
		if (tracked) {
			body[i]->GetJoints(JointType_Count, joints);
			break;
		}
	}

	if (bodyframe) bodyframe->Release();
}


void getKinectData() {
	IMultiSourceFrame* frame = NULL;
	if (SUCCEEDED(reader->AcquireLatestFrame(&frame))) {
		getDepthData(frame);
		getRgbData(frame);
		getBodyData(frame);
	}
	if (frame) frame->Release();
}


void controlClickCallback(int event, int x, int y, int flags, void* userdata) {
	if (event == EVENT_LBUTTONDOWN) {
		if (record_button.contains(Point(x, y))) {
			if (!recording) {
				cout << "Start recording..." << endl;
				recording = true;
			}
			else {
				cout << "Stopped!" << endl;
				stopped = true;
			}
		}
	}
}


void makeControlPanel() {
	string recButtonText("Click to record");
	Mat3b img(300, 300, Vec3b(0, 255, 0));
	record_button = Rect(0, 0, img.cols, 50);
	ctrl_canvas = Mat3b(img.rows + record_button.height, img.cols, Vec3b(0, 0, 0));
	ctrl_canvas(record_button) = Vec3b(200, 200, 200);
	putText(ctrl_canvas(record_button), recButtonText, Point(record_button.width*0.25,
		record_button.height*0.7), FONT_HERSHEY_PLAIN, 1, Scalar(0, 0, 0));
	img.copyTo(ctrl_canvas(Rect(0, record_button.height, img.cols, img.rows)));
	namedWindow(controlWindowName);
	setMouseCallback(controlWindowName, controlClickCallback);
	imshow(controlWindowName, ctrl_canvas);
}


void drawArm(Mat& fullRGBimg) {
	const int jtCnt = 3;
	CameraSpacePoint cameraPoints[jtCnt];
	ColorSpacePoint colorPoints[jtCnt];
	cameraPoints[0] = joints[JointType_ShoulderLeft].Position;
	cameraPoints[1] = joints[JointType_ElbowLeft].Position;
	cameraPoints[2] = joints[JointType_WristLeft].Position;
	mapper->MapCameraPointsToColorSpace(jtCnt, cameraPoints, jtCnt, colorPoints);
	Point ls(int(colorPoints[0].X), int(colorPoints[0].Y));
	Point le(int(colorPoints[1].X), int(colorPoints[1].Y));
	Point lw(int(colorPoints[2].X), int(colorPoints[2].Y));
	circle(fullRGBimg, ls, 10, Vec3b(0, 0, 255), 5);
	circle(fullRGBimg, le, 10, Vec3b(255, 0, 255), 5);
	circle(fullRGBimg, lw, 10, Vec3b(255, 255, 0), 5);
	line(fullRGBimg, ls, le, Vec3b(255, 255, 255), 5);
	line(fullRGBimg, le, lw, Vec3b(255, 255, 255), 5);
}

int main()
{
	
	loadHSVRange(&H_MIN, &H_MAX, &S_MIN, &S_MAX, &V_MIN, &V_MAX);
	makeControlPanel();

	Mat fullRGBimg;
	Mat HSVimg;

	//waitKey(0.1);

	if (!initKinect()) return 1;
	recording = false;
	bool marker = true;
	bool markerFound = false;
	try {
		while (1) {
			getKinectData();
			fullRGBimg = Mat(c_height, c_width, CV_8UC4, (void *)rgbimage);
			if (marker) {
				if (markerFound) {
					// local search
					
				}
				else {
					// full search
					
				}
			}
			if (recording) {
				if (marker) {
					if (markerFound) {
						// convert marker pos to XYZ
						// write marker data
					}
					else {
						// no marker
					}
				}
				// write body data
			}
			else {
				if (tracked) {
					drawArm(fullRGBimg);
				}
				if (marker && markerFound) {
					// draw marker

				}
				// display
				imshow(checkWindowName, fullRGBimg);
				
			}
			waitKey(5);
			if (stopped) {
				cout << "saving recorded data..." << endl;
				stopped = false;
				recording = false;
				waitKey(1000);
			}
		}
	}
	catch (cv::Exception & e) {
		cerr << e.msg << endl; // output exception message
	}
	// save recorded data to txt file
    return 0;
}

