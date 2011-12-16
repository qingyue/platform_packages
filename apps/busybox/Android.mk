LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_POST_PROCESS_COMMAND := $(shell cp -r $(LOCAL_PATH)/bin/*  $(TARGET_OUT)/bin)
LOCAL_POST_PROCESS_COMMAND := $(shell cp -r $(LOCAL_PATH)/sbin/*  $(TARGET_OUT)/xbin)
