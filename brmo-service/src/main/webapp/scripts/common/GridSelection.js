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
 * Class to create a grid with selection possiblities and a button to save selection to backend
 */
Ext.define('B3P.common.GridSelection', {
    // Default config
    config: {
        fields: [],
        columns: [],
        gridUrl: '',
        actionUrl: '',
        gridSaveUrl: '',
        buttonTitle: '',
        gridId: '',
        commentId: '',
        autoRefresh: true,
        autoRefreshTime: 60000
    },
    store: null,
    grid: null,
    // Autorefresh timer to make sure refreshing only happens once after each load
    refreshTimer: null,
    commentBox: null,
    constructor: function(config) {
        // Reference to object
        var me = this;
        var gridItemId = Ext.id();
        var pagerItemId = Ext.id();
        var maxResults = -1;
        var movingBack = 0;
        var pageSize = 40;
        // Ext function to initialize configuration
        this.initConfig(config);
        // Create the data model
        var model = Ext.define('GridSelectionModel', {
            extend: 'Ext.data.Model',
            fields: this.config.fields,
            proxy: {
                type: 'ajax',
                url: this.config.gridSaveUrl,
                writer: {
                    type: 'json',
                    rootProperty: 'changedItem',
                    encode: true
                }
            }
        });
        // Create the store
        this.store = Ext.create('Ext.data.Store', {
            model: model,
            autoLoad: false,
            pageSize: pageSize,
            remoteSort: true,
            remoteFilter: true,
            proxy: {
                type: 'ajax',
                url: this.config.gridUrl,
                reader: {
                    type: 'json',
                    root: 'items',
                    totalProperty: 'total'
                },
                writer: {
                    type: 'json',
                    rootProperty: 'changedItem',
                    encode: true
                },
                simpleSortMode: true
            },
            listeners: {
                load: function(store, records, successful) {
                    if(!successful) {
                        me.setMessage('error', "Fout bij ophalen van gegevens, auto refresh functie gestopt");
                        return;
                    }
                    if(me.config.autoRefresh) {
                        me.refreshTimer && clearTimeout(me.refreshTimer);
                        me.refreshTimer = setTimeout(function() {
                            store.load();
                        }, me.config.autoRefreshTime);   
                    }
                    var pager = Ext.ComponentQuery.query("#" + pagerItemId)[0];
                    /**
                     * We will disable the 'last page' button of the pagertoolbar when we have a virtual total
                     */
                    if (store.getProxy().getReader().rawData.hasOwnProperty('virtualtotal') && store.getProxy().getReader().rawData.virtualtotal) {
                        // Kind of hack to disable 'last page' button, property will be used on 'afterlayout' event on pager, see below
                        pager.hideLastButton = true;
                    }
                    /**
                     * When the store hits the end of a resultset (total is unknown at the beginning)
                     * the total is stored and set to correct number after each load
                     */
                    if (maxResults !== -1) {
                        store.totalCount = maxResults;
                        if (pager) {
                            pager.onLoad();  // triggers correct total
                        }
                    }
                    /**
                     * total is not 0 but there are not records (resultset has exactly x pages of pageSize)
                     * store total and move to previous page
                     */
                    if (store.totalCount !== 0 && records.length === 0) {
                        maxResults = store.totalCount;
                        if (store.currentPage !== 0 && movingBack < 4) {
                            movingBack++;
                            store.loadPage(parseInt(store.totalCount / pageSize, 10));
                        }
                    }
                    /**
                     * the number of results are less than the pageSize so we are at the end of the set
                     */
                    if (store.totalCount !== 0 && records.length < pageSize) {
                        maxResults = store.totalCount;
                    }

                    setTimeout(function(){ Ext.ComponentQuery.query("#" + gridItemId)[0].updateLayout(); }, 0);
                }
            }
        });
        // Create grid
        this.grid = Ext.create('Ext.grid.Panel', {
            store: this.store,
            itemId: gridItemId,
            selType: 'checkboxmodel',
            plugins: [{
                ptype: 'gridfilters'
            },{
                ptype: 'cellediting',
                clicksToEdit: 1
            },{
                ptype : 'pagingselectpersist'
            }],
            xtype: 'grid-filtering',
            requires: [
                'Ext.grid.filters.Filters'
            ],
            columns: this.config.columns,
            dockedItems: [{
                itemId: pagerItemId,
                xtype: 'pagingtoolbar',
                store: this.store,
                dock: 'bottom',
                displayInfo: true,
                listeners: {
                    afterlayout: function() {
                        if(this.hideLastButton) {
                            this.child('#last').disable();
                        }
                    }
                }
            }],
            renderTo: this.config.gridId,
            listeners: {
                beforeedit: function() {
                    me.refreshTimer && clearTimeout(me.refreshTimer);
                },
                edit: function(editor, item) {
                    item.record.save({
                        callback: function() {
                            me.reloadGrid();
                        }
                    });
                },
                canceledit: function() {
                    me.reloadGrid();
                }
            }
        });
        this.reloadGrid();
        this.grid.getSelectionModel().setSelectionMode('SIMPLE');
        // Create action button
        Ext.create('Ext.Button', {
            text: this.config.buttonTitle,
            renderTo: this.config.buttonId,
            handler: function() {
                var selection = me.grid.getPlugin('pagingSelectionPersistence').getPersistedSelection();
                if(selection.length === 0) {
                    return;
                }
                var selectedIds = [];
                for(var i = 0; i < selection.length; i++) {
                    selectedIds.push(selection[i].id);
                }
                Ext.Ajax.request({
                    url: me.config.actionUrl,
                    success: function(response) {
                        me.setMessage('success', response.responseText);
                        me.reloadGrid();
                    },
                    failure: function(response) {
                        me.setMessage('error', response.responseText);
                    },
                    params: {
                        selectedIds: selectedIds
                    }
                });
            }
        });
    },
    reloadGrid: function() {
        this.store.load();
    },
    createMessagebox: function() {
        var me = this;
        // Create message box
        this.commentBox = (function(commentId) {
            // Create DOM nodes
            var commentbox = document.getElementById(commentId);
            commentbox.className = 'messagebox';
            // Create header container
            var headerContainer = document.createElement('div');
            commentbox.appendChild(headerContainer);
            // Create header
            var header = document.createElement('span');
            header.className = 'header';
            headerContainer.appendChild(header);
            // Close button
            var messageclose = document.createElement('a');
            messageclose.href = '#';
            messageclose.innerHTML = '&times;';
            messageclose.className = 'close';
            headerContainer.appendChild(messageclose);
            // Expand button
            var messageexpand = document.createElement('a');
            messageexpand.href = '#';
            messageexpand.innerHTML = '&#9660;';
            messageexpand.className = 'expand';
            headerContainer.appendChild(messageexpand);
            // Content area
            var content = document.createElement('span');
            content.className = 'content';
            commentbox.appendChild(content);
            // Add listeners
            Ext.get(messageclose).addListener('click', function() {
                commentbox.className = commentbox.className.replace(' visible', '');
            });
            Ext.get(messageexpand).addListener('click', function() {
                if(commentbox.className.indexOf('expanded') !== -1) {
                    messageexpand.innerHTML = '&#9660;';
                    commentbox.className = commentbox.className.replace(' expanded', '');
                } else {
                    commentbox.className += ' expanded';
                    messageexpand.innerHTML = '&#9650;';
                }
            });
            // Return object with setMessage function to expose to outside
            return {
                setMessage: function(type, message) {
                    commentbox.className = 'messagebox visible ' + type;
                    header.innerHTML = type === 'success' ? 'Actie succesvol uitgevoerd' : 'Er zijn fouten opgetreden';
                    content.innerHTML = message;
                }
            };
        }(this.config.commentId));
    },
    setMessage: function(type, message) {
        if(this.commentBox === null) {
            this.createMessagebox();
        }
        this.commentBox.setMessage(type, message);
    }
});