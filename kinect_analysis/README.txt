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
	마커 트래킹을 위한 HSV range를 실험적으로 결정하는 용도

kinectTracker3.cpp
	마커, 관절 녹화 메인코드.
	현재 하나의 마커, 왼손목, 왼팔꿈치, 왼어깨, 어깨중심을 트래킹하지만,
	관절은 아주 쉽게 추가 가능하며 마커는 약간 더 까다롭지만 퍼포먼스가 허용하는 한 확장가능

kindata.txt
	녹화하면 저장되는 숫자 텍스트 파일

parse_kindata.m
	kindata.txt를 MATLAB에서 사용가능한 kdat.mat으로 바꾸는 스크립트

kdat.mat
	parse_kindata.m의 결과물. saved data에 모아둠

visualize_kindata.m
	kdat.mat을 간단한 애니메이션으로 보여주는 스크립트. 녹화 직후 확인용으로 좋음

gyro_analysis/main.m
	선재형이 작성한 워치 센서로부터 위치를 추정하는 스크립트

gypos.mat
	main.m을 돌렸을 때 추정되는 위치와 타임스텝을 저장해둔 오브젝트. amateur_gyro에 일부 모아둠

pendulum3.m
	kdat.mat과 gypos.mat를 비교분석하는 스크립트.
	같은 폴더 내에서 정의된 함수를 많이 이용함
	
<중요한 함수들>
scalarTimeSync():
	한 쪽이 다른 한쪽을 포함하는 두 스칼라 시그널이 주어졌을 때 가장 잘 맞아떨어질때의 샘플 오프셋을 구해줌
	그냥 timeSync()는 방향이 안 맞는 3차원벡터의 sequence로 비슷한기능 함 (속력이용해서 맞춤)
fitRotationMatrix():
	방향이 안 맞는 3차원 유닛벡터의 sequence 두개가 들어오면, 두 sequence사이의 적절한 회전변환 행렬 1개을 반환
