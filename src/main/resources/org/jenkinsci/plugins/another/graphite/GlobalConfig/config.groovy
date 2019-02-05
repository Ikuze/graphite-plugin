package org.jenkinsci.plugins.another.graphite.GlobalConfig;

import org.jenkinsci.plugins.another.graphite.GlobalConfig;


def f = namespace(lib.FormTagLib);

f.section(title: descriptor.displayName) {
    f.entry(title: _("Graphite Servers"),
            help: descriptor.getHelpFile()) {
        
        f.repeatableHeteroProperty(
                field: "servers",
                hasHeader: "true",
                addCaption: _("Add Graphite Server"))
    }

        
    f.entry(field: "baseQueueName") {
        f.textbox()
    }
}
