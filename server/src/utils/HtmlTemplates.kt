package me.nekoalice.mafia.api.server.utils

import kotlinx.html.*

internal fun HTML.getTelegramLoginSuccessHtml(authCode: String) {
    head {
        link(
            rel = "stylesheet",
            href = "https://cdn.jsdelivr.net/npm/water.css@2/out/water.css",
        )
        meta(charset = "utf-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title("Login successful")
    }
    body {
        h1 {
            +"Login successful"
        }
        label {
            +"Your login code:"
            input(type = InputType.text) {
                id = "login-code"
                readonly = true
                value = authCode
            }
        }
        button(type = ButtonType.button) {
            id = "copy"
            +"Copy"
        }
        p {
            id = "close-tab-hint"
            style = "display: none;"
            +"Now you can close this tab"
        }
        script {
            unsafe {
                // language="JavaScript"
                raw(
                    """
                        document.getElementById("copy").addEventListener("click", function () {
                            navigator.clipboard.writeText(document.getElementById("login-code").value).then(() => {
                                document.getElementById("close-tab-hint").style = ""
                                const originalInnerText = this.innerText
                                this.innerText = "Copied!"
                                this.disabled = true
                                setTimeout(() => {
                                    this.disabled = false
                                    this.innerText = originalInnerText
                                }, 2000)
                            })          
                        })
                    """.trimIndent(),
                )
            }
        }
    }
}
