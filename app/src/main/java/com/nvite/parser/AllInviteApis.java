package com.nvite.parser;

/**
 * Class for all possible constants for application
 */
public class AllInviteApis {

    // https://docs.google.com/spreadsheets/d/17etYb9K6bUFEg6nmHSZBS3snPIx2B_v1kZk_TEH-Kks/edit#gid=5050523
    public static String BASE_ADMIN_URL = "http://35.167.245.101/invite/";
//    public static String BASE_ADMIN_URL = "http://202.164.42.226/staging/invite/";

    public static String LOGIN_URL = BASE_ADMIN_URL + "login";
    public static String SIGNUP_URL = BASE_ADMIN_URL + "signup";
    public static String SOCIAL_LOGIN = BASE_ADMIN_URL + "social_login";
    public static String RESEND_OTP = BASE_ADMIN_URL + "resent_otp";
    public static String VERIFY_OTP = BASE_ADMIN_URL + "verify_otp";
    public static String FORGOT_PASSWORD = BASE_ADMIN_URL + "forgot_password";
    public static String CHANGE_PASSWORD = BASE_ADMIN_URL + "change_password";
    public static String PHONE_VERIFY = BASE_ADMIN_URL + "phone_verfiy";
    public static String ENTER_USERNAME = BASE_ADMIN_URL + "check_username";
    public static String LOGOUT = BASE_ADMIN_URL + "logout";
    public static String RECENT_EVENT = BASE_ADMIN_URL + "recent_event_list";
    public static String MY_EVENT = BASE_ADMIN_URL + "my_event_list";
    public static String UPDATE_PROFILE = BASE_ADMIN_URL + "update_profile";
    public static String FRIEND_LIST = BASE_ADMIN_URL + "friend_list";
    public static String SAVED_EVENT_LIST = BASE_ADMIN_URL + "save_event_list";
    public static String PRIVATE_EVENT_LIST = BASE_ADMIN_URL + "invite_event_listing";
    public static String CREATE_EVENT = BASE_ADMIN_URL + "create_event";
    public static String EVENT_LISTING = BASE_ADMIN_URL + "event_listing";
    public static String CONTACT_LIST = BASE_ADMIN_URL + "contact_list";
    public static String SAVE_EVENT = BASE_ADMIN_URL + "save_event";
    public static String ATTEND_EVENT = BASE_ADMIN_URL + "attend_event";
    public static String ATTEND_EVENT_LIST = BASE_ADMIN_URL + "event_attending_user";
    public static String INVITED_EVENT_LIST = BASE_ADMIN_URL + "event_invited_user";
    public static String GET_PROFILE = BASE_ADMIN_URL + "GetProfileDetails";
    public static String SEARCH_FRIEND_LIST = BASE_ADMIN_URL + "searchUser";
    public static String ADD_FRIEND = BASE_ADMIN_URL + "addfriend";
    public static String SEND_FRIEND_REQUEST = BASE_ADMIN_URL + "send_friend_request";
    public static String NOTIFICATION = BASE_ADMIN_URL + "notification";
    public static String EVENT_DETAIL = BASE_ADMIN_URL + "event_detail";
    public static String ACCEPT_FRIEND_REQUEST = BASE_ADMIN_URL + "accept_friend_request";
    public static String SEND_INVITATION = BASE_ADMIN_URL + "send_invitation";
    public static String GET_CHAT_EVENTS = BASE_ADMIN_URL + "get_chat_events";
    public static String GET_CHAT_GROUPS = BASE_ADMIN_URL + "get_groups";
    public static String CREATE_GROUP = BASE_ADMIN_URL + "group_create";
    public static String COUNT_NOTIFICATION = BASE_ADMIN_URL + "count_notification";
    public static String SEEN_NOTIFICATION = BASE_ADMIN_URL + "seen_notification";
    public static String UNSAVE_EVENT = BASE_ADMIN_URL + "unsave_event";
    public static String GET_CHAT = BASE_ADMIN_URL + "get_chat";
    public static String GET_EVENT_CHAT = BASE_ADMIN_URL + "get_event_chat";
    public static String EVENT_CHAT = BASE_ADMIN_URL + "event_chat";
    public static String GROUP_CHAT = BASE_ADMIN_URL + "chat";
    public static String MSG_SEEN = BASE_ADMIN_URL + "msg_seen";
    public static String GROUP_MEMBERS = BASE_ADMIN_URL + "group_members";
    public static String LEFT_GROUP = BASE_ADMIN_URL + "left_group";
    public static String EDIT_EVENT = BASE_ADMIN_URL + "edit_event";
    public static String DELETE_EVENT = BASE_ADMIN_URL + "delete_event";
    public static String REMOVE_FRIEND = BASE_ADMIN_URL + "remove_friend";
    public static String QR_CHECK = BASE_ADMIN_URL + "qr_check";
    public static String UPDATE_LOCATION = BASE_ADMIN_URL + "update_location";

}
