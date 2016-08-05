/*
 * Copyright (C) 2011-2016 B3Partners B.V.
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
 * Laadproces grid
 */
Ext.define('B3P.brmo.LaadProces', {
    constructor: function(config) {
        Ext.onReady(function() {
            var gridSelection = Ext.create('B3P.common.GridSelection', {
                fields: [
                    {name: 'id', type: 'int'},
                    {name: 'bestand_naam', type: 'string'},
                    {name: 'bestand_datum', type: 'string'},
                    {name: 'soort', type: 'string'},
                    {name: 'status', type: 'string'}
                ],
                columns: [
                    {text: "id", dataIndex: 'id'},
                    {text: "bestand_naam", dataIndex: 'bestand_naam', flex: 1},
                    {text: "bestand_datum", dataIndex: 'bestand_datum'},
                    {text: "soort", dataIndex: 'soort', filter: 'string'},
                    {text: "status", dataIndex: 'status', filter: 'string',
                        editor: {
                            xtype: 'combobox',
                            store: ['STAGING_OK']
                        },
                        flex: 1},
                    {
                        text: "log",
                        dataIndex: 'id',
                        flex: 1,
                        renderer: function (value) {
                            return Ext.String.format('<a href="#" onclick="return openLog({0});" title="Open opmerkingen"><img src="images/page_text.gif"/></a>', value);
                        }
                    }
                ],
                gridUrl: config.gridurl,
                gridSaveUrl: config.gridsaveurl,
                actionUrl: config.deleteurl,
                buttonTitle: 'Verwijderen',
                gridId: 'laadproces-grid',
                buttonId: 'button-delete',
                commentId: 'comment-div'
            });
            Ext.create('Ext.Button', {
                text: 'Selectie transformeren naar RSGB',
                renderTo: 'button-transform',
                handler: function() {
                    var ids = gridSelection.grid.getSelection();
                    if (ids.length === 0) {
                        Ext.Msg.alert('Selectie transformeren', 'Geen laadprocessen geselecteerd!');
                        return;
                    }
                    var p = "";
                    for(var i = 0; i < ids.length; i++) {
                        if(p !== "") {
                            p += "&";
                        }
                        p += "selectedIds=" + ids[i].id;
                    }
                    window.open(config.transformurl + "&" + p);
                }
            });
            Ext.create('Ext.Button', {
                text: 'Selectie versneld transformeren naar RSGB (alleen stand)',
                renderTo: 'button-transform-stand',
                handler: function() {
                    var ids = gridSelection.grid.getSelection();
                    if (ids.length === 0) {
                        Ext.Msg.alert('Selectie transformeren', 'Geen laadprocessen geselecteerd!');
                        return;
                    }
                    var p = "";
                    for(var i = 0; i < ids.length; i++) {
                        if(p !== "") {
                            p += "&";
                        }
                        p += "selectedIds=" + ids[i].id;
                    }
                    window.open(config.transformstandurl + "&" + p);
                }
            });
        });
        this.logUrl = config.logurl;
    }
});
var logWindow = null;
function openLog(id) {
    var url = b3plaadprocessen.logUrl + "&selectedIds=" + id;
    Ext.Ajax.request({
        url: url,
        callback: function (options, success, response) {
            if (logWindow) {
                logWindow.destroy();
            }
            var bericht = Ext.decode(response.responseText);
            var text = bericht.opmerking;
            var html = "<pre>" + text + "</pre>";
            logWindow = Ext.create('Ext.window.Window', {
                title: 'Log van ' + bericht.id + ": " + bericht.bestand_naam,
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