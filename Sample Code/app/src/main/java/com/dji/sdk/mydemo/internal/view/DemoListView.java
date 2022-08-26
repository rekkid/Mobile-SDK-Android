package com.dji.sdk.mydemo.internal.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;

import com.dji.sdk.mydemo.R;
import com.dji.sdk.mydemo.demo.accessory.AccessoryAggregationView;
import com.dji.sdk.mydemo.demo.accessory.AudioFileListManagerView;
import com.dji.sdk.mydemo.demo.airlink.RebootWiFiAirlinkView;
import com.dji.sdk.mydemo.demo.airlink.SetGetOcuSyncLinkView;
import com.dji.sdk.mydemo.demo.airlink.SetGetWiFiLinkSSIDView;
import com.dji.sdk.mydemo.demo.appactivation.AppActivationView;
import com.dji.sdk.mydemo.demo.battery.PushBatteryDataView;
import com.dji.sdk.mydemo.demo.battery.SetGetDischargeDayView;
import com.dji.sdk.mydemo.demo.camera.CameraCalibration;
import com.dji.sdk.mydemo.demo.camera.FetchMediaView;
import com.dji.sdk.mydemo.demo.camera.LiveStreamView;
import com.dji.sdk.mydemo.demo.camera.MediaPlaybackView;
import com.dji.sdk.mydemo.demo.camera.MultipleLensCameraView;
import com.dji.sdk.mydemo.demo.camera.PlaybackCommandsView;
import com.dji.sdk.mydemo.demo.camera.PlaybackDownloadView;
import com.dji.sdk.mydemo.demo.camera.PlaybackPushInfoView;
import com.dji.sdk.mydemo.demo.camera.PushCameraDataView;
import com.dji.sdk.mydemo.demo.camera.RecordVideoView;
import com.dji.sdk.mydemo.demo.camera.SetGetISOView;
import com.dji.sdk.mydemo.demo.camera.ShootSinglePhotoView;
import com.dji.sdk.mydemo.demo.camera.ShootSuperResolutionPhotoView;
import com.dji.sdk.mydemo.demo.camera.VideoFeederView;
import com.dji.sdk.mydemo.demo.camera.XT2CameraView;
import com.dji.sdk.mydemo.demo.datalocker.AccessLockerView;
import com.dji.sdk.mydemo.demo.firmwareupgrade.FirmwareUpgradeView;
import com.dji.sdk.mydemo.demo.flightcontroller.CompassCalibrationView;
import com.dji.sdk.mydemo.demo.flightcontroller.FlightAssistantPushDataView;
import com.dji.sdk.mydemo.demo.flightcontroller.FlightHubView;
import com.dji.sdk.mydemo.demo.flightcontroller.FlightLimitationView;
import com.dji.sdk.mydemo.demo.flightcontroller.NetworkRTKView;
import com.dji.sdk.mydemo.demo.flightcontroller.OrientationModeView;
import com.dji.sdk.mydemo.demo.flightcontroller.VirtualStickView;
import com.dji.sdk.mydemo.demo.gimbal.GimbalCapabilityView;
import com.dji.sdk.mydemo.demo.gimbal.MoveGimbalWithSpeedView;
import com.dji.sdk.mydemo.demo.gimbal.PushGimbalDataView;
import com.dji.sdk.mydemo.demo.key.KeyedInterfaceView;
import com.dji.sdk.mydemo.demo.keymanager.KeyManagerView;
import com.dji.sdk.mydemo.demo.lidar.LidarView;
import com.dji.sdk.mydemo.demo.lookat.LookAtMissionView;
import com.dji.sdk.mydemo.demo.missionoperator.FollowMeMissionOperatorView;
import com.dji.sdk.mydemo.demo.missionoperator.WaypointMissionOperatorView;
import com.dji.sdk.mydemo.demo.missionoperator.WaypointV2MissionOperatorView;
import com.dji.sdk.mydemo.demo.mobileremotecontroller.MobileRemoteControllerView;
import com.dji.sdk.mydemo.demo.mydemo.BasicInfoView;
import com.dji.sdk.mydemo.demo.radar.RadarView;
import com.dji.sdk.mydemo.demo.remotecontroller.DualRemoteControllerView;
import com.dji.sdk.mydemo.demo.remotecontroller.PushRemoteControllerDataView;
import com.dji.sdk.mydemo.demo.timeline.TimelineMissionControlView;
import com.dji.sdk.mydemo.demo.useraccount.LDMView;
import com.dji.sdk.mydemo.internal.controller.DJISampleApplication;
import com.dji.sdk.mydemo.internal.controller.ExpandableListAdapter;
import com.dji.sdk.mydemo.internal.controller.MainActivity;
import com.dji.sdk.mydemo.internal.model.GroupHeader;
import com.dji.sdk.mydemo.internal.model.GroupItem;
import com.dji.sdk.mydemo.internal.model.ListItem;
import com.squareup.otto.Subscribe;

/**
 * This view is in charge of showing all the demos in a list.
 */

public class DemoListView extends FrameLayout {

    private ExpandableListAdapter listAdapter;
    private ExpandableListView expandableListView;

    public DemoListView(Context context) {
        this(context, null, 0);
    }

    public DemoListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.demo_list_view, this);

        // Build model for ListView
        ListItem.ListBuilder builder = new ListItem.ListBuilder();
        builder.addGroup(R.string.component_listview_MyDemo,
                false,
                new GroupItem(R.string.component_listview_MyDemo_BasicInfo, BasicInfoView.class),
                new GroupItem(R.string.component_listview_MyDemo_VirtualStick, VirtualStickView.class),
                new GroupItem(R.string.component_listview_MyDemo_Live, LiveStreamView.class),
                new GroupItem(R.string.component_listview_MyDemo_WayPoint, WaypointMissionOperatorView.class));

        builder.addGroup(R.string.component_listview_sdk_4_16,
                false,
                new GroupItem(R.string.look_at_mission, LookAtMissionView.class));
        builder.addGroup(R.string.component_listview_sdk_4_15,
                false,
                new GroupItem(R.string.component_listview_lidar_view, LidarView.class));
        builder.addGroup(R.string.component_listview_sdk_4_14,
                false,
                new GroupItem(R.string.component_listview_radar, RadarView.class),
                new GroupItem(R.string.component_listview_ldm, LDMView.class));
        builder.addGroup(R.string.component_listview_sdk_4_12,
                false,
                new GroupItem(R.string.component_listview_multiple_lens_camera, MultipleLensCameraView.class),
                new GroupItem(R.string.component_listview_health_information, HealthInformationView.class),
                new GroupItem(R.string.component_listview_waypointv2, WaypointV2MissionOperatorView.class),
                new GroupItem(R.string.component_listview_utmiss, StartUTMISSActivityView.class));
        builder.addGroup(R.string.component_listview_sdk_4_11,
                false,
                new GroupItem(R.string.component_listview_firmware_upgrade, FirmwareUpgradeView.class));
        builder.addGroup(R.string.component_listview_sdk_4_9,
                false,
                new GroupItem(R.string.component_listview_live_stream, LiveStreamView.class));
        builder.addGroup(R.string.component_listview_sdk_4_8,
                false,
                new GroupItem(R.string.component_listview_access_locker, AccessLockerView.class),
                new GroupItem(R.string.component_listview_accessory_aggregation,
                        AccessoryAggregationView.class),
                new GroupItem(R.string.component_listview_audio_file_list_manager,
                        AudioFileListManagerView.class));
        builder.addGroup(R.string.component_listview_sdk_4_6,
                false,
                new GroupItem(R.string.component_listview_payload,
                        StartPayloadAcitivityView.class),
                new GroupItem(R.string.component_listview_redirect_to_djigo,
                        StartRedirectGoAcitivityView.class));
        builder.addGroup(R.string.component_listview_sdk_4_5,
                false,
                new GroupItem(R.string.component_listview_flight_hub,
                        FlightHubView.class));

        builder.addGroup(R.string.component_listview_sdk_4_1,
                false,
                new GroupItem(R.string.component_listview_app_activation,
                        AppActivationView.class));


        builder.addGroup(R.string.component_listview_sdk_4_0,
                false,
                new GroupItem(R.string.component_listview_waypoint_mission_operator,
                        WaypointMissionOperatorView.class),
                new GroupItem(R.string.component_listview_follwome_mission_operator,
                        FollowMeMissionOperatorView.class),
                new GroupItem(R.string.component_listview_keyed_interface, KeyedInterfaceView.class),
                new GroupItem(R.string.component_listview_timeline_mission_control,
                        TimelineMissionControlView.class));

        builder.addGroup(R.string.component_listview_key_manager, false,
                new GroupItem(R.string.key_manager_listview_key_Interface, KeyManagerView.class));

        builder.addGroup(R.string.component_listview_camera,
                false,
                new GroupItem(R.string.camera_listview_push_info, PushCameraDataView.class),
                new GroupItem(R.string.camera_listview_iso, SetGetISOView.class),
                new GroupItem(R.string.camera_listview_shoot_single_photo, ShootSinglePhotoView.class),
                new GroupItem(R.string.camera_listview_shoot_super_resolutiob_photo, ShootSuperResolutionPhotoView.class),
                new GroupItem(R.string.camera_listview_record_video, RecordVideoView.class),
                new GroupItem(R.string.camera_listview_playback_push_info, PlaybackPushInfoView.class),
                new GroupItem(R.string.camera_listview_playback_command, PlaybackCommandsView.class),
                new GroupItem(R.string.camera_listview_playback_download, PlaybackDownloadView.class),
                new GroupItem(R.string.camera_listview_download_media, FetchMediaView.class),
                new GroupItem(R.string.camera_listview_media_playback, MediaPlaybackView.class),
                new GroupItem(R.string.component_listview_video_feeder, VideoFeederView.class),
                new GroupItem(R.string.component_xt2_camera_view, XT2CameraView.class),
                new GroupItem(R.string.camera_calibration, CameraCalibration.class));

        builder.addGroup(R.string.component_listview_gimbal,
                false,
                new GroupItem(R.string.gimbal_listview_push_info, PushGimbalDataView.class),
                new GroupItem(R.string.gimbal_listview_rotate_gimbal, MoveGimbalWithSpeedView.class),
                new GroupItem(R.string.gimbal_listview_gimbal_capability, GimbalCapabilityView.class));

        builder.addGroup(R.string.component_listview_battery,
                false,
                new GroupItem(R.string.battery_listview_push_info, PushBatteryDataView.class),
                new GroupItem(R.string.battery_listview_set_get_discharge_day, SetGetDischargeDayView.class));

        builder.addGroup(R.string.component_listview_airlink,
                false,
                new GroupItem(R.string.airlink_listview_wifi_set_get_ssid, SetGetWiFiLinkSSIDView.class),
                new GroupItem(R.string.airlink_listview_wifi_reboot_wifi, RebootWiFiAirlinkView.class),
                new GroupItem(R.string.airlink_listview_lb_set_get_channel, SetGetWiFiLinkSSIDView.class),
                new GroupItem(R.string.airlink_listview_ocusync_set_get_channel, SetGetOcuSyncLinkView.class));

        builder.addGroup(R.string.component_listview_flight_controller,
                false,
                new GroupItem(R.string.flight_controller_listview_compass_calibration,
                        CompassCalibrationView.class),
                new GroupItem(R.string.flight_controller_listview_flight_limitation,
                        FlightLimitationView.class),
                new GroupItem(R.string.flight_controller_listview_orientation_mode, OrientationModeView.class),
                new GroupItem(R.string.flight_controller_listview_virtual_stick, VirtualStickView.class),
                new GroupItem(R.string.flight_controller_listview_intelligent_flight_assistant,
                        FlightAssistantPushDataView.class),
                new GroupItem(R.string.flight_controller_listview_networkRTK, NetworkRTKView.class));

        builder.addGroup(R.string.component_listview_remote_controller,
                false,
                new GroupItem(R.string.remote_controller_listview_push_info,
                        PushRemoteControllerDataView.class),
                new GroupItem(R.string.component_listview_mobile_remote_controller,
                        MobileRemoteControllerView.class),
                new GroupItem(R.string.component_listview_dual_remote_controller,
                        DualRemoteControllerView.class));

        // Set-up ExpandableListView
        expandableListView = (ExpandableListView) view.findViewById(R.id.expandable_list);
        listAdapter = new ExpandableListAdapter(context, builder.build());
        expandableListView.setAdapter(listAdapter);
        DJISampleApplication.getEventBus().register(this);
        expandAllGroupIfNeeded();
    }

    @Subscribe
    public void onSearchQueryEvent(MainActivity.SearchQueryEvent event) {
        listAdapter.filterData(event.getQuery());
        expandAllGroup();
    }

    /**
     * Expands all the group that has children
     */
    private void expandAllGroup() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            expandableListView.expandGroup(i);
        }
    }

    /**
     * Expands all the group that has children
     */
    private void expandAllGroupIfNeeded() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            if (listAdapter.getGroup(i) instanceof GroupHeader
                    && ((GroupHeader) listAdapter.getGroup(i)).shouldCollapseByDefault()) {
                expandableListView.collapseGroup(i);
            } else {
                expandableListView.expandGroup(i);
            }
        }
    }
}
