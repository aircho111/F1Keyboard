public class DrivingController {	
	public class DrivingCmd{
		public double steer;
		public double accel;
		public double brake;
		public int backward;
	};

	public DrivingCmd controlDriving(double[] driveArray, double[] aicarArray, double[] trackArray, double[] damageArray, int[] rankArray, int trackCurveType, double[] trackAngleArray, double[] trackDistArray, double trackCurrentAngle){
		DrivingCmd cmd = new DrivingCmd();
		
		////////////////////// input parameters
		double toMiddle     = driveArray[DrivingInterface.drvie_toMiddle    ];
		double angle        = driveArray[DrivingInterface.drvie_angle       ];
		double speed        = driveArray[DrivingInterface.drvie_speed       ];

		double toStart				 = trackArray[DrivingInterface.track_toStart		];
		double dist_track			 = trackArray[DrivingInterface.track_dist_track		];
		double track_width			 = trackArray[DrivingInterface.track_width			];
		double track_dist_straight	 = trackArray[DrivingInterface.track_dist_straight	];
		int track_curve_type		 = trackCurveType;

		double[] track_forward_angles	= trackAngleArray;
		double[] track_forward_dists	= trackDistArray;
		double track_current_angle		= trackCurrentAngle;
		
		double[] dist_cars = aicarArray;
		
		double damage		 = damageArray[DrivingInterface.damage];
		double damage_max	 = damageArray[DrivingInterface.damage_max];

		int total_car_num	 = rankArray[DrivingInterface.rank_total_car_num	];
		int my_rank			 = rankArray[DrivingInterface.rank_my_rank			];
		int opponent_rank	 = rankArray[DrivingInterface.rank_opponent_rank	];		
		////////////////////// END input parameters
		
		
		System.out.println("======================== start ================================");

		double user_brakeCtl    = 0.0;
		
		double user_steerCtl    = 0.0;
		double user_steer_coeff = 0.541052; // 핸들계수(트랙정보에 따라 셋팅) 
		double user_dist_center = toMiddle;
		
		// 트랙 코스에 따른 처리
		if(track_curve_type == 1){ // 우회전 
			user_dist_center = toMiddle + ( track_width/5 );  
		}else if(track_curve_type == 2){ // 좌회전
			user_dist_center = toMiddle - ( track_width/5 ); 
		}else{
			user_dist_center = toMiddle;
		}

		double user_accelCtl    = 0.0;
		double user_v 		    = 0.0;
		float user_v_max        = 50; // 최고속도(트랙정보에 따라 셋팅)
		float user_c_coeff      = (float)2.772;
		float user_d_coeff		= (float)-0.693;
		float user_dist_cars    = 10;
		
		System.out.println("track_curve_type : " + track_curve_type);
		System.out.println("toMiddle : " + toMiddle);
		System.out.println("user_dist_center : " + user_dist_center);
		System.out.println("angle : " + angle);
		System.out.println("track_width : " + track_width);	
		
		// 최적속도
		user_v = user_v_max * (1 - Math.exp(-user_c_coeff/user_v_max * user_dist_cars - user_d_coeff));
		
		System.out.println("user_v : " + user_v);
		

		if(speed > 20){
			System.out.println("speed > 0: " + speed);
			if(speed > user_v){
				user_accelCtl = 0.0;
				
			}else if(speed < user_v){
				user_accelCtl = 0.5;
			}
		}else{
			System.out.println("speed < 0: " + speed);
			user_accelCtl = 0.3;
		}
		
		
		
		// 핸들조작
		user_steerCtl = user_steer_coeff * (angle - user_dist_center/track_width);
		
		System.out.println("user_accelCtl : " + user_accelCtl);
		System.out.println("user_steerCtl : " + user_steerCtl);
		
		System.out.println("======================== end ================================");
		
		////////////////////// output values		
		cmd.steer = user_steerCtl;
		cmd.accel = user_accelCtl;
		cmd.brake = user_brakeCtl;
		cmd.backward = DrivingInterface.gear_type_forward;
		////////////////////// END output values
		
		return cmd;
	}
	
	public static void main(String[] args) {
		DrivingInterface driving = new DrivingInterface();
		DrivingController controller = new DrivingController();
		
		double[] driveArray = new double[DrivingInterface.INPUT_DRIVE_SIZE];
		double[] aicarArray = new double[DrivingInterface.INPUT_AICAR_SIZE];
		double[] trackArray = new double[DrivingInterface.INPUT_TRACK_SIZE];
		double[] damageArray = new double[DrivingInterface.INPUT_DAMAGE_SIZE];
		int[] rankArray = new int[DrivingInterface.INPUT_RANK_SIZE];
		int[] trackCurveType = new int[1];
		double[] trackAngleArray = new double[DrivingInterface.INPUT_FORWARD_TRACK_SIZE];
		double[] trackDistArray = new double[DrivingInterface.INPUT_FORWARD_TRACK_SIZE];
		double[] trackCurrentAngle = new double[1];
				
		// To-Do : Initialize with your team name.
		int result = driving.OpenSharedMemory();
		
		if(result == 0){
			boolean doLoop = true;
			while(doLoop){
				result = driving.ReadSharedMemory(driveArray, aicarArray, trackArray, damageArray, rankArray, trackCurveType, trackAngleArray, trackDistArray, trackCurrentAngle);
				switch(result){
				case 0:
					DrivingCmd cmd = controller.controlDriving(driveArray, aicarArray, trackArray, damageArray, rankArray, trackCurveType[0], trackAngleArray, trackDistArray, trackCurrentAngle[0]);
					driving.WriteSharedMemory(cmd.steer, cmd.accel, cmd.brake, cmd.backward);
					break;
				case 1:
					break;
				case 2:
					// disconnected
				default:
					// error occurred
					doLoop = false;
					break;
				}
			}
		}
	}
}
