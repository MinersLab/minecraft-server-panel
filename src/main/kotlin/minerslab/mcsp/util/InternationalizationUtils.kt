package minerslab.mcsp.util

import com.vaadin.flow.component.login.LoginI18n
import com.vaadin.flow.component.upload.UploadI18N
import com.vaadin.flow.component.upload.UploadI18N.Uploading

val LOGIN_I18N: LoginI18n =
    LoginI18n.createDefault().apply {
        errorMessage.apply {
            title = "登录失败"
            username = "未输入用户名"
            password = "未输入密码"
            message = "请检查您是否输入了正确的用户名和密码，然后重试。"
        }
        form.apply {
            title = "登录"
            password = "密码"
            username = "用户名"
            submit = "提交"
        }
    }

val UPLOAD_I18N =
    UploadI18N().apply {
        uploading =
            Uploading().apply {
                status =
                    Uploading.Status().apply {
                        stalled = "已暂停"
                        connecting = "连接中"
                        processing = "处理中"
                        held = "等待中"
                    }
                error =
                    Uploading.Error().apply {
                        forbidden = "禁止访问"
                        serverUnavailable = "服务器不可用"
                        unexpectedServerError = "服务器错误"
                    }
                remainingTime =
                    Uploading.RemainingTime().apply {
                        prefix = "时间剩余: "
                        unknown = "未知"
                    }
            }
        error =
            UploadI18N.Error().apply {
                fileIsTooBig = "文件过大"
                tooManyFiles = "文件过多"
                incorrectFileType = "文件类型错误"
            }
        file =
            UploadI18N.File().apply {
                start = "开始"
                remove = "移除"
                retry = "重试"
            }
        addFiles =
            UploadI18N.AddFiles().apply {
                one = "上传一个文件"
                many = "上传多个文件"
            }
        dropFiles =
            UploadI18N.DropFiles().apply {
                one = "拖动一个文件并上传"
                many = "拖动多个文件并上传"
            }
    }
