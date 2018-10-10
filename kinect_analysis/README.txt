Kinect Smartwatch
 - Mostly developed by Eunhyouk Shin (Email silvershine157@kaist.ac.kr to ask)

<Directory Structure>
HSVfiltering
	objectTrackingTut
kinectTracker3
	kinectTracker3
		kinectTracker3.cpp
		kindata.txt
		parse_kindata.m
		kdat.mat
		saved data
			.../kdat.mat
		visualize_kindata.m
		gyro_analysis
			main.m
			.../gypos.mat
		pendulum3.m
		

objectTrackingTut:
	��Ŀ Ʈ��ŷ�� ���� HSV range�� ���������� �����ϴ� �뵵

kinectTracker3.cpp
	��Ŀ, ���� ��ȭ �����ڵ�.
	���� �ϳ��� ��Ŀ, �޼ո�, ���Ȳ�ġ, �޾��, ����߽��� Ʈ��ŷ������,
	������ ���� ���� �߰� �����ϸ� ��Ŀ�� �ణ �� ��ٷ����� �����ս��� ����ϴ� �� Ȯ�尡��

kindata.txt
	��ȭ�ϸ� ����Ǵ� ���� �ؽ�Ʈ ����

parse_kindata.m
	kindata.txt�� MATLAB���� ��밡���� kdat.mat���� �ٲٴ� ��ũ��Ʈ

kdat.mat
	parse_kindata.m�� �����. saved data�� ��Ƶ�

visualize_kindata.m
	kdat.mat�� ������ �ִϸ��̼����� �����ִ� ��ũ��Ʈ. ��ȭ ���� Ȯ�ο����� ����

gyro_analysis/main.m
	�������� �ۼ��� ��ġ �����κ��� ��ġ�� �����ϴ� ��ũ��Ʈ

gypos.mat
	main.m�� ������ �� �����Ǵ� ��ġ�� Ÿ�ӽ����� �����ص� ������Ʈ. amateur_gyro�� �Ϻ� ��Ƶ�

pendulum3.m
	kdat.mat�� gypos.mat�� �񱳺м��ϴ� ��ũ��Ʈ.
	���� ���� ������ ���ǵ� �Լ��� ���� �̿���
	
<�߿��� �Լ���>
scalarTimeSync():
	�� ���� �ٸ� ������ �����ϴ� �� ��Į�� �ñ׳��� �־����� �� ���� �� �¾ƶ��������� ���� �������� ������
	�׳� timeSync()�� ������ �� �´� 3���������� sequence�� ����ѱ�� �� (�ӷ��̿��ؼ� ����)
fitRotationMatrix():
	������ �� �´� 3���� ���ֺ����� sequence �ΰ��� ������, �� sequence������ ������ ȸ����ȯ ��� 1���� ��ȯ
