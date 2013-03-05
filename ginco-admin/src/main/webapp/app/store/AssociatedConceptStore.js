/*
 * ReducedConcept Store
 * This file contains all ReducedConcept displayed in popup lists
 */
Ext.define('GincoApp.store.AssociatedConceptStore', {
    extend: 'Ext.data.Store',

    constructor: function(cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: 'services/ui/thesaurusconceptservice/getSimpleConcepts',
                reader: {
                    type: 'json',
                    idProperty: 'identifier',
                    root: 'data'
                }
            },
            fields: [
                {
                    name : 'identifier',
                    type : 'string'
                },
                {
                    name: 'label',
                    type : 'string'
                },
                {
                    name: 'lang',
                    type : 'string'
                }
            ]

        }, cfg)]);
    }
});