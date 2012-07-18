LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_POST_PROCESS_COMMAND := $(shell cp -r $(LOCAL_PATH)/*.apk  $(TARGET_OUT)/app/)
LOCAL_POST_PROCESS_COMMAND := $(shell cp -r $(LOCAL_PATH)/lib/*.so  $(TARGET_OUT)/lib/)