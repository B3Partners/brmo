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
Ext.define('B3P.brmo.Berichten', {
    constructor: function(config) {
        Ext.onReady(function() {
            // Create the grid
            var gridSelection = Ext.create('B3P.common.GridSelection', {
                fields: [
                    {name: 'id', type: 'int'},
                    {name: 'object_ref', type: 'string'},
                    {name: 'volgordenummer', type: 'int'},
                    {name: 'datum', type: 'string'},
                    {name: 'soort', type: 'string', persist: true},
                    {name: 'status', type: 'string'}
                ],
                columns: [
                    {text: "id", dataIndex: 'id'},
                    {text: "ref", dataIndex: 'object_ref', flex: 3},
                    {text: "volgordenummer", dataIndex: 'volgordenummer', flex: 1},
                    {text: "datum", dataIndex: 'datum'},
                    {text: "soort", dataIndex: 'soort', filter: 'string'},
                    {
                        text: "status",
                        dataIndex: 'status',
                        filter: {
                            type: 'list',
                            options: ['STAGING_OK', 'STAGING_NOK', 'STAGING_FORWARDED', 'RSGB_WAITING', 'RSGB_PROCESSING', 'RSGB_OK', 'RSGB_NOK', 'RSGB_BAG_NOK', 'RSGB_OUTDATED', 'ARCHIVE']
                        },
                        editor: {
                            xtype: 'combobox',
                            store: ['STAGING_OK', 'RSGB_WAITING']
                        },
                        flex: 2
                    },{
                        text: "log",
                        dataIndex: 'id',
                        flex: 1,
                        renderer: function(value) {
                           return Ext.String.format('<a href="#" onclick="return openLog({0});" title="Open opmerkingen"><img src="images/page_text.gif"/></a>', value);
                        }
                   }
                ],
                gridUrl: config.gridurl,
                gridSaveUrl: config.gridsaveurl,
                buttonTitle: 'Selectie naar RSGB',
                gridId: 'berichten-grid',
                commentId: 'comment-div'

            });
            Ext.create('Ext.Button', {
                text: 'Selectie transformeren naar RSGB',
                renderTo: 'button-run',
                handler: function() {
                    var ids = gridSelection.grid.getSelection();
                    if(ids.length === 0) {
                        Ext.Msg.alert('Selectie transformeren', 'Geen berichten geselecteerd!');
                        return;
                    }
                    var p = "";
                    for(var i = 0; i < ids.length; i++) {
                        if(p !== "") {
                            p += "&";
                        }
                        p += "selectedIds=" + ids[i].id;
                    }
                    window.open(config.runurl + "&" + p);
                }
            });
            Ext.create('Ext.Button', {
                text: 'Alles transformeren naar RSGB',
                renderTo: 'button-run-all',
                handler: function() {
                    window.open(config.runallurl);
                }
            });
            Ext.create('Ext.Button', {
                text: 'Versneld transformeren naar RSGB (alleen stand)',
                renderTo: 'button-run-all-stand',
                handler: function() {
                    window.open(config.runallstandurl);
                }
            });
            Ext.create('Ext.Button', {
                text: 'WAITING-berichten nogmaals transformeren naar RSGB',
                renderTo: 'button-retry',
                handler: function() {
                    window.open(config.retryurl);
                }
            });
            Ext.create('Ext.Button', {
                text: 'WAITING-berichten nogmaals transformeren naar RSGB (alleen stand)',
                renderTo: 'button-retry-stand',
                handler: function() {
                    window.open(config.retrystandurl);
                }
            });
            Ext.create('Ext.Button', {
                text: 'Geavanceerde functies',
                renderTo: 'button-advanced-functions',
                handler: function() {
                    window.open(config.advancedfunctionsurl);
                }
            });
            Ext.create('Ext.Button', {
                text: 'Snelle updates...',
                renderTo: 'button-run-updates',
                handler: function() {
                    window.open(config.runupdatesurl);
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
            var bericht = Ext.decode(response.responseText);
            var text = bericht.opmerking;
            var html = "<pre>" + text +"</pre>";
            logWindow = Ext.create('Ext.window.Window', {
                title: 'Log van ' + bericht.id +": " + bericht.object_ref,
                height: 400,
                width: 700,
                autoScroll:true,
                layout: 'fit',
                html: html
            });
            logWindow.show();
        }
    });
}