package org.jenkinsci.plugins.another.graphite.servers.Server


import org.jenkinsci.plugins.another.graphite.servers.Server

def f = namespace(lib.FormTagLib);

f.entry(title: _("ID"), field: "id") {
    f.textbox()
}

f.entry(title: _("IP/Host"), field: "ip") {
    f.textbox()
}

f.entry(title: _("Port"), field: "port") {
    f.textbox()
}

f.block() {
    f.validateButton(
            title: _("Test connection"),
            progress: _("Testing..."),
            method: "testConnection",
            with: "ip,port"
    )
}


