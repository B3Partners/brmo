/*
 * Copyright (C) 2011-2014 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Berichten grid
 */
Ext.define('B3P.brmo.NHRNummers', {
    constructor: function(config) {
        Ext.onReady(function() {
            // Create the grid
            var gridSelection = Ext.create('B3P.common.GridSelection', {
                fields: [
                    {name: 'kvkNummer', type: 'string'},
                    {name: 'datum', type: 'string'},
                    {name: 'laatstGeprobeerd', type: 'string'},
                    {name: 'volgendProberen', type: 'string'},
                    {name: 'probeerAantal', type: 'int'}
                ],
                columns: [
                    {text: "kvkNummer", dataIndex: 'kvkNummer'},
                    {text: "datum", dataIndex: 'datum'},
                    {text: 'laatst geprobeerd', dataIndex: 'laatstGeprobeerd'},
                    {text: 'volgend proberen', dataIndex: 'volgendProberen'},
                    {text: "probeerAantal", dataIndex: 'probeerAantal'},
                    {
                        text: "log",
                        dataIndex: 'kvkNummer',
                        renderer: function(value) {
                           return Ext.String.format('<a href="#" onclick="return openLog(\'{0}\');" title="Open log"><img src="images/page_text.gif"/></a>', value);
                        }
                   }
                ],
                gridUrl: config.gridurl,
                gridId: 'nhr-grid',
                commentId: 'comment-div'
            });

            Ext.create('Ext.Button', {
                text: 'Inschrijvingen nu opnieuw ophalen',
                renderTo: 'button-retry',
                handler: function() {
                    var ids = gridSelection.grid.getSelection();
                    if(ids.length === 0) {
                        Ext.Msg.alert('Inschrijvingen ophalen', 'Geen vestigingen geselecteerd!');
                        return;
                    }

                    var p = "";
                    for(var i = 0; i < ids.length; i++) {
                        if(p !== "") {
                            p += "&";
                        }
                        p += "selectedIds=" + ids[i].data.kvkNummer;
                    }

                    Ext.Ajax.request({
                        url: config.runurl + "&" + p,
                        callback: function(options, success, response) {
                            if (response.responseText != "ok") {
                                Ext.Msg.alert('Inschrijvingen ophalen', response.responseText);
                            }
                            gridSelection.reloadGrid();
                        }
                    });
                }
            });
        });
        this.logUrl = config.logurl;
    }
});
var logWindow = null;
function openLog(id) {
    var url = b3pberichten.logUrl + "&selectedIds=" + id;
    Ext.Ajax.request({
        url: url,
        callback: function(options, success, response) {
            if(logWindow){
                logWindow.destroy();
            }
            var bericht = response.responseText;
            var html = "<pre>" + bericht.replace(/</g, "&lt;") + "</pre>";
            logWindow = Ext.create('Ext.window.Window', {
                title: 'Log van ' + id,
                height: 400,
                width: 700,
                autoScroll: true,
                layout: 'fit',
                html: html
            });
            logWindow.show();
        }
    });
}
