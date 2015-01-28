/**
 * Grid PagingSelectionPersistence plugin
 * 
 * Maintains row selection state when moving between pages of a paginated grid
 *
 * Public Methods:
 * getPersistedSelection() - retrieve the array of selected records across all pages
 * clearPersistedSelection() - deselect records across all pages
 * 
 *
 * @class   Ext.ux.grid.plugin.PagingSelectionPersistence
 * @extends Ext.AbstractPlugin
 * @author  Bill Dami
 * @date    December 20th, 2011
 */
Ext.define('Ext.ux.grid.plugin.PagingSelectionPersistence', {
    alias: 'plugin.pagingselectpersist',
    extend: 'Ext.AbstractPlugin',
    pluginId: 'pagingSelectionPersistence',
    
    //array of selected records
    selection: [],
    //hash map of record id to selected state
    selected: {},
    
    init: function(grid) {
        this.grid = grid;
        this.selModel = this.grid.getSelectionModel();
        this.isCheckboxModel = (this.selModel.$className == 'Ext.selection.CheckboxModel');
        this.origOnHeaderClick = this.selModel.onHeaderClick;
        this.bindListeners();
    },
    
    destroy: function() {
        this.selection = [];
        this.selected = {};
        this.disable();
    },
    
    enable: function() {
        var me = this;
        
        if(this.disabled && this.grid) {
            this.grid.getView().on('refresh', this.onViewRefresh, this);
            this.selModel.on('select', this.onRowSelect, this);
            this.selModel.on('deselect', this.onRowDeselect, this);
            
            if(this.isCheckboxModel) {
                //For CheckboxModel, we need to detect when the header deselect/select page checkbox
                //is clicked, to make sure the plugin's selection array is updated. This is because Ext.selection.CheckboxModel
                //interally supresses event firings for selectAll/deselectAll when its clicked
                this.selModel.onHeaderClick = function(headerCt, header, e) {
                    var isChecked = header.el.hasCls(Ext.baseCSSPrefix + 'grid-hd-checker-on');
                    me.origOnHeaderClick.apply(this, arguments);
                    
                    if(isChecked) {
                        me.onDeselectPage();
                    } else {
                        me.onSelectPage();
                    }
                };
            }
        }
    
        this.callParent();
    },
    
    disable: function() {
        if(this.grid) {
            this.grid.getView().un('refresh', this.onViewRefresh, this);
            this.selModel.un('select', this.onRowSelect, this);
            this.selModel.un('deselect', this.onRowDeselect, this);
            this.selModel.onHeaderClick = this.origOnHeaderClick;
        }


        this.callParent();
    },
    
    bindListeners: function() {
        var disabled = this.disabled;
        
        this.disable();
        
        if(!disabled) {
            this.enable();
        }
    },
    
    onViewRefresh : function(view, eOpts) {
        var store = this.grid.getStore(),
            sel = [],
            hdSelectState,
            rec,
            i;
        
        this.ignoreChanges = true;
        
        for(i = store.getCount() - 1; i >= 0; i--) {
            rec = store.getAt(i);
            
            if(this.selected[rec.getId()]) {
                sel.push(rec);
            }
        }
        
        this.selModel.select(sel, false);

        this.ignoreChanges = false;
    },
    
    onRowSelect: function(sm, rec, idx, eOpts) {
        if(this.ignoreChanges === true) {
            return;
        }
        
        if(!this.selected[rec.getId()]) 
        {
            this.selection.push(rec);
            this.selected[rec.getId()] = true;
        }
    },
    
    onRowDeselect: function(sm, rec, idx, eOpts) {
        var i;
        
        if(this.ignoreChanges === true) {
            return;
        }
        
        if(this.selected[rec.getId()])
        {
            for(i = this.selection.length - 1; i >= 0; i--) {
                if(this.selection[i].getId() == rec.getId()) {
                    this.selection.splice(i, 1);
                    this.selected[rec.getId()] = false;
                    break;
                }
            }
        }
    },
    
    onSelectPage: function() {
        var sel = this.selModel.getSelection(),
            len = this.getPersistedSelection().length,
            i;
        
        for(i = 0; i < sel.length; i++) {
            this.onRowSelect(this.selModel, sel[i]);
        }
        
        if(len !== this.getPersistedSelection().length) {
            this.selModel.fireEvent('selectionchange', this.selModel, [], {});
        }
    },
    
    onDeselectPage: function() {
        var store = this.grid.getStore(),
            len = this.getPersistedSelection().length,
            i;
        
        for(i = store.getCount() - 1; i >= 0; i--) {
            this.onRowDeselect(this.selModel, store.getAt(i));
        }
        
        if(len !== this.getPersistedSelection().length) {
            this.selModel.fireEvent('selectionchange', this.selModel, [], {});
        }
    },
    
    getPersistedSelection: function() {
        return [].concat(this.selection);
    },
    
    clearPersistedSelection: function() {
        var changed = (this.selection.length > 0);
        
        this.selection = [];
        this.selected = {};
        this.onViewRefresh();
        
        if(changed) {
            this.selModel.fireEvent('selectionchange', this.selModel, [], {});
        }
    }
});