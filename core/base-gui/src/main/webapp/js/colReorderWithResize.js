/*
* File:        ColReorderWithResize-1.1.0-dev2.js
* Version:     1.1.0-dev2 (based on commit 2a6de4e884 done on Feb 22, 2013)
* CVS:         $Id$
* Description: Allow columns to be reordered in a DataTable
* Author:      Allan Jardine (www.sprymedia.co.uk)
* Created:     Wed Sep 15 18:23:29 BST 2010
* Modified:    2013 feb 2013 by nlz242
* Language:    Javascript
* License:     GPL v2 or BSD 3 point style
* Project:     DataTables
* Contact:     www.sprymedia.co.uk/contact
*
* Copyright 2010-2013 Allan Jardine, all rights reserved.
*
* This source file is free software, under either the GPL v2 license or a
* BSD style license, available at:
*   http://datatables.net/license_gpl2
*   http://datatables.net/license_bsd
*
* Minor bug fixes by Jeremy Hubble @jeremyhubble
*/


(function ($, window, document) {


    /**
    * Switch the key value pairing of an index array to be value key (i.e. the old value is now the
    * key). For example consider [ 2, 0, 1 ] this would be returned as [ 1, 2, 0 ].
    *  @method  fnInvertKeyValues
    *  @param   array aIn Array to switch around
    *  @returns array
    */
    function fnInvertKeyValues(aIn) {
        var aRet = [];
        for (var i = 0, iLen = aIn.length; i < iLen; i++) {
            aRet[aIn[i]] = i;
        }
        return aRet;
    }


    /**
    * Modify an array by switching the position of two elements
    *  @method  fnArraySwitch
    *  @param   array aArray Array to consider, will be modified by reference (i.e. no return)
    *  @param   int iFrom From point
    *  @param   int iTo Insert point
    *  @returns void
    */
    function fnArraySwitch(aArray, iFrom, iTo) {
        var mStore = aArray.splice(iFrom, 1)[0];
        aArray.splice(iTo, 0, mStore);
    }


    /**
    * Switch the positions of nodes in a parent node (note this is specifically designed for
    * table rows). Note this function considers all element nodes under the parent!
    *  @method  fnDomSwitch
    *  @param   string sTag Tag to consider
    *  @param   int iFrom Element to move
    *  @param   int Point to element the element to (before this point), can be null for append
    *  @returns void
    */
    function fnDomSwitch(nParent, iFrom, iTo) {
        var anTags = [];
        for (var i = 0, iLen = nParent.childNodes.length; i < iLen; i++) {
            if (nParent.childNodes[i].nodeType == 1) {
                anTags.push(nParent.childNodes[i]);
            }
        }
        var nStore = anTags[iFrom];

        if (iTo !== null) {
            nParent.insertBefore(nStore, anTags[iTo]);
        }
        else {
            nParent.appendChild(nStore);
        }
    }



    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    * DataTables plug-in API functions
    *
    * This are required by ColReorder in order to perform the tasks required, and also keep this
    * code portable, to be used for other column reordering projects with DataTables, if needed.
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


    /**
    * Plug-in for DataTables which will reorder the internal column structure by taking the column
    * from one position (iFrom) and insert it into a given point (iTo).
    *  @method  $.fn.dataTableExt.oApi.fnColReorder
    *  @param   object oSettings DataTables settings object - automatically added by DataTables!
    *  @param   int iFrom Take the column to be repositioned from this point
    *  @param   int iTo and insert it into this point
    *  @returns void
    */
    $.fn.dataTableExt.oApi.fnColReorder = function (oSettings, iFrom, iTo) {
        var i, iLen, j, jLen, iCols = oSettings.aoColumns.length, nTrs, oCol;

        /* Sanity check in the input */
        if (iFrom == iTo) {
            /* Pointless reorder */
            return;
        }

        if (iFrom < 0 || iFrom >= iCols) {
            this.oApi._fnLog(oSettings, 1, "ColReorder 'from' index is out of bounds: " + iFrom);
            return;
        }

        if (iTo < 0 || iTo >= iCols) {
            this.oApi._fnLog(oSettings, 1, "ColReorder 'to' index is out of bounds: " + iTo);
            return;
        }

        /*
        * Calculate the new column array index, so we have a mapping between the old and new
        */
        var aiMapping = [];
        for (i = 0, iLen = iCols; i < iLen; i++) {
            aiMapping[i] = i;
        }
        fnArraySwitch(aiMapping, iFrom, iTo);
        var aiInvertMapping = fnInvertKeyValues(aiMapping);


        /*
        * Convert all internal indexing to the new column order indexes
        */
        /* Sorting */
        for (i = 0, iLen = oSettings.aaSorting.length; i < iLen; i++) {
            oSettings.aaSorting[i][0] = aiInvertMapping[oSettings.aaSorting[i][0]];
        }

        /* Fixed sorting */
        if (oSettings.aaSortingFixed !== null) {
            for (i = 0, iLen = oSettings.aaSortingFixed.length; i < iLen; i++) {
                oSettings.aaSortingFixed[i][0] = aiInvertMapping[oSettings.aaSortingFixed[i][0]];
            }
        }

        /* Data column sorting (the column which the sort for a given column should take place on) */
        for (i = 0, iLen = iCols; i < iLen; i++) {
            oCol = oSettings.aoColumns[i];
            for (j = 0, jLen = oCol.aDataSort.length; j < jLen; j++) {
                oCol.aDataSort[j] = aiInvertMapping[oCol.aDataSort[j]];
            }
        }


        /*
        * Move the DOM elements
        */
        if (oSettings.aoColumns[iFrom].bVisible) {
            /* Calculate the current visible index and the point to insert the node before. The insert
            * before needs to take into account that there might not be an element to insert before,
            * in which case it will be null, and an appendChild should be used
            */
            var iVisibleIndex = this.oApi._fnColumnIndexToVisible(oSettings, iFrom);
            var iInsertBeforeIndex = null;

            i = iTo < iFrom ? iTo : iTo + 1;
            while (iInsertBeforeIndex === null && i < iCols) {
                iInsertBeforeIndex = this.oApi._fnColumnIndexToVisible(oSettings, i);
                i++;
            }

            /* Header */
            nTrs = oSettings.nTHead.getElementsByTagName('tr');
            for (i = 0, iLen = nTrs.length; i < iLen; i++) {
                fnDomSwitch(nTrs[i], iVisibleIndex, iInsertBeforeIndex);
            }

            /* Footer */
            if (oSettings.nTFoot !== null) {
                nTrs = oSettings.nTFoot.getElementsByTagName('tr');
                for (i = 0, iLen = nTrs.length; i < iLen; i++) {
                    fnDomSwitch(nTrs[i], iVisibleIndex, iInsertBeforeIndex);
                }
            }

            /* Body */
            for (i = 0, iLen = oSettings.aoData.length; i < iLen; i++) {
                if (oSettings.aoData[i].nTr !== null) {
                    fnDomSwitch(oSettings.aoData[i].nTr, iVisibleIndex, iInsertBeforeIndex);
                }
            }
        }


        /*
        * Move the internal array elements
        */
        /* Columns */
        fnArraySwitch(oSettings.aoColumns, iFrom, iTo);

        /* Search columns */
        fnArraySwitch(oSettings.aoPreSearchCols, iFrom, iTo);

        /* Array array - internal data anodes cache */
        for (i = 0, iLen = oSettings.aoData.length; i < iLen; i++) {
            fnArraySwitch(oSettings.aoData[i]._anHidden, iFrom, iTo);
        }

        /* Reposition the header elements in the header layout array */
        for (i = 0, iLen = oSettings.aoHeader.length; i < iLen; i++) {
            fnArraySwitch(oSettings.aoHeader[i], iFrom, iTo);
        }

        if (oSettings.aoFooter !== null) {
            for (i = 0, iLen = oSettings.aoFooter.length; i < iLen; i++) {
                fnArraySwitch(oSettings.aoFooter[i], iFrom, iTo);
            }
        }


        /*
        * Update DataTables' event handlers
        */

        /* Sort listener */
        for (i = 0, iLen = iCols; i < iLen; i++) {
            $(oSettings.aoColumns[i].nTh).off('click');
            this.oApi._fnSortAttachListener(oSettings, oSettings.aoColumns[i].nTh, i);
        }


        /* Fire an event so other plug-ins can update */
        $(oSettings.oInstance).trigger('column-reorder', [oSettings, {
            "iFrom": iFrom,
            "iTo": iTo,
            "aiInvertMapping": aiInvertMapping
        }]);

        if (typeof oSettings.oInstance._oPluginFixedHeader != 'undefined') {
            oSettings.oInstance._oPluginFixedHeader.fnUpdate();
        }
    };




    /**
    * ColReorder provides column visibility control for DataTables
    * @class ColReorder
    * @constructor
    * @param {object} dt DataTables settings object
    * @param {object} opts ColReorder options
    */
    var ColReorder = function (dt, opts) {
                $(dt.nTableWrapper).width($(dt.nTable).width());
                // make sure the headers are the same width as the rest of table
                dt.aoDrawCallback.push({
                    "fn": function ( oSettings ) {
                        $(oSettings.nTableWrapper).width($(oSettings.nTable).width());
                    },
                    "sName": "Resize headers"
                });
        var oDTSettings;
        // Set the table to minimum size so that it doesn't stretch too far
        $(dt.nTable).width("10px");


        // @todo - This should really be a static method offered by DataTables
        if (dt.fnSettings) {
            // DataTables object, convert to the settings object
            oDTSettings = dt.fnSettings();
        }
        else if (typeof dt === 'string') {
            // jQuery selector
            if ($.fn.dataTable.fnIsDataTable($(dt)[0])) {
                oDTSettings = $(dt).eq(0).dataTable().fnSettings();
            }
        }
        else if (dt.nodeName && dt.nodeName.toLowerCase() === 'table') {
            // Table node
            if ($.fn.dataTable.fnIsDataTable(dt.nodeName)) {
                oDTSettings = $(dt.nodeName).dataTable().fnSettings();
            }
        }
        else if (dt instanceof jQuery) {
            // jQuery object
            if ($.fn.dataTable.fnIsDataTable(dt[0])) {
                oDTSettings = dt.eq(0).dataTable().fnSettings();
            }
        }
        else {
            // DataTables settings object
            oDTSettings = dt;
        }

        if (this instanceof ColReorder === false) {
            // Get a ColReorder instance - effectively a static method
            for (var i = 0, iLen = ColReorder.aoInstances.length; i < iLen; i++) {
                if (ColReorder.aoInstances[i].s.dt == oDTSettings) {
                    return ColReorder.aoInstances[i];
                }
            }

            return null;
        }

        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
        * Public class variables
        * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

        /**
        * @namespace Settings object which contains customisable information for ColReorder instance
        */
        this.s = {
            /**
            * DataTables settings object
            *  @property dt
            *  @type     Object
            *  @default  null
            */
            "dt": null,

            /**
            * Initialisation object used for this instance
            *  @property init
            *  @type     object
            *  @default  {}
            */
            "init": $.extend(true, {}, ColReorder.defaults, opts),

            /**
            * Allow Reorder functionnality
            *  @property allowReorder
            *  @type     boolean
            *  @default  true
            */
            "allowReorder": true,

            /**
            * Expand or collapse columns based on header double clicks
            *  @property allowHeaderDoubleClick
            *  @type     boolean
            *  @default  true
            */
            "allowHeaderDoubleClick": true,

            /**
            * Expand or collapse columns based on header double clicks
            * If set to true will use the default menu
            * - If set to false, no context menu will be used
            * - If set to true, the default context menu will be used
            * - If given a function, that function will be called
            *  @property headerContextMenu
            *  @type     boolean/function
            *  @default  true
            */
            "headerContextMenu": true,

            /**
            * Allow Resize functionnality
            *  @property allowResize
            *  @type     boolean
            *  @default  true
            */
            "allowResize": true,

            /**
            * Number of columns to fix (not allow to be reordered)
            *  @property fixed
            *  @type     int
            *  @default  0
            */
            "fixed": 0,

            /**
            * Number of columns to fix counting from right (not allow to be reordered)
            *  @property fixedRight
            *  @type     int
            *  @default  0
            */
            "fixedRight": 0,

            /**
            * Callback function for once the reorder has been done
            *  @property dropcallback
            *  @type     function
            *  @default  null
            */
            "dropCallback": null,

            /**
            * @namespace Information used for the mouse drag
            */
            "mouse": {
                "startX": -1,
                "startY": -1,
                "offsetX": -1,
                "offsetY": -1,
                "target": -1,
                "targetIndex": -1,
                "fromIndex": -1
            },

            /**
            * Information which is used for positioning the insert cusor and knowing where to do the
            * insert. Array of objects with the properties:
            *   x: x-axis position
            *   to: insert point
            *  @property aoTargets
            *  @type     array
            *  @default  []
            */
            "aoTargets": [],

            /**
            * Minimum width for columns (in pixels)
            * Default is 10. If set to 0, columns can be resized to nothingness.
            * @property minResizeWidth
            * @type     integer
            * @default  10
            */
            "minResizeWidth": 10,

            /**
            * Resize the table when columns are resized
            * @property bResizeTable
            * @type     bolean
            * @default  true
            */
            "bResizeTable": true,


            /**
            * Callback called after each time the table is resized
            * This could be multiple times on one mouse move.
            * useful for resizing a containing element.
            * Passed the table element, new size, and the size change
            * @property fnResizeTableCallback
            * @type     function
            * @default  function(table, newSize, sizeChange) {}
            */
            "fnResizeTableCallback": function(){},

            /**
            * Add table-layout:fixed css to the table
            * This header is required for column resize to function properly
            * However, in some cases, you may want to do additional processing, and thus not set the header
            * (For example, you may want the headers to be layed out normally, and then fix the table
            *  after the headers are allocated their full space. In this case, you can manually add the css
            *  in fnHeaderCallback and set bAddFixed to false here)
            * @property bAddFixed
            * @type     bolean
            * @default  true
            */
            "bAddFixed": true
        };


        /**
        * @namespace Common and useful DOM elements for the class instance
        */
        this.dom = {
            /**
            * Dragging element (the one the mouse is moving)
            *  @property drag
            *  @type     element
            *  @default  null
            */
            "drag": null,

            /**
            * Resizing a column
            *  @property drag
            *  @type     element
            *  @default  null
            */
            "resize": null,

            /**
            * The insert cursor
            *  @property pointer
            *  @type     element
            *  @default  null
            */
            "pointer": null
        };

        this.table_size = -1;

        /* Constructor logic */
        this.s.dt = oDTSettings.oInstance.fnSettings();
        this._fnConstruct();

        /* Add destroy callback */
        oDTSettings.oApi._fnCallbackReg(oDTSettings, 'aoDestroyCallback', $.proxy(this._fnDestroy, this), 'ColReorder');

        /* Store the instance for later use */
        ColReorder.aoInstances.push(this);


        // fix the width and add table layout fixed. 
        if (this.s.bAddFixed) {
            $(this.s.dt.nTable).width($(this.s.dt.nTable).width()).css('table-layout','fixed');
        }
        return this;
    };



    ColReorder.prototype = {
        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
        * Public methods
        * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

        /**
        * Reset the column ordering to the original ordering that was detected on
        * start up.
        *  @return {this} Returns `this` for chaining.
        *
        *  @example
        *    // DataTables initialisation with ColReorder
        *    var table = $('#example').dataTable( {
        *        "sDom": 'Rlfrtip'
        *    } );
        *
        *    // Add click event to a button to reset the ordering
        *    $('#resetOrdering').click( function (e) {
        *        e.preventDefault();
        *        $.fn.dataTable.ColReorder( table ).fnReset();
        *    } );
        */
        "fnReset": function () {
            var a = [];
            for (var i = 0, iLen = this.s.dt.aoColumns.length; i < iLen; i++) {
                a.push(this.s.dt.aoColumns[i]._ColReorder_iOrigCol);
            }

            this._fnOrderColumns(a);

            return this;
        },

        /**
        * `Deprecated` - Get the current order of the columns, as an array.
        *  @return {array} Array of column identifiers
        *  @deprecated `fnOrder` should be used in preference to this method.
        *      `fnOrder` acts as a getter/setter.
        */
        "fnGetCurrentOrder": function () {
            return this.fnOrder();
        },

        /**
        * Get the current order of the columns, as an array. Note that the values
        * given in the array are unique identifiers for each column. Currently
        * these are the original ordering of the columns that was detected on
        * start up, but this could potentially change in future.
        *  @return {array} Array of column identifiers
        *
        *  @example
        *    // Get column ordering for the table
        *    var order = $.fn.dataTable.ColReorder( dataTable ).fnOrder();
        */
        /**
        * Set the order of the columns, from the positions identified in the
        * ordering array given. Note that ColReorder takes a brute force approach
        * to reordering, so it is possible multiple reordering events will occur
        * before the final order is settled upon.
        *  @param {array} [set] Array of column identifiers in the new order. Note
        *    that every column must be included, uniquely, in this array.
        *  @return {this} Returns `this` for chaining.
        *
        *  @example
        *    // Swap the first and second columns
        *    $.fn.dataTable.ColReorder( dataTable ).fnOrder( [1, 0, 2, 3, 4] );
        *
        *  @example
        *    // Move the first column to the end for the table `#example`
        *    var curr = $.fn.dataTable.ColReorder( '#example' ).fnOrder();
        *    var first = curr.shift();
        *    curr.push( first );
        *    $.fn.dataTable.ColReorder( '#example' ).fnOrder( curr );
        *
        *  @example
        *    // Reverse the table's order
        *    $.fn.dataTable.ColReorder( '#example' ).fnOrder(
        *      $.fn.dataTable.ColReorder( '#example' ).fnOrder().reverse()
        *    );
        */
        "fnOrder": function (set) {
            if (set === undefined) {
                var a = [];
                for (var i = 0, iLen = this.s.dt.aoColumns.length; i < iLen; i++) {
                    a.push(this.s.dt.aoColumns[i]._ColReorder_iOrigCol);
                }
                return a;
            }

            this._fnOrderColumns(fnInvertKeyValues(set));

            return this;
        },

        /**
        * fnGetColumnSelectList - return html list of columns columns, with selected columns checked
        *  @return {string} Html string
        */
        fnGetColumnSelectList : function() {

            var tp,i;
            var availableFields = this.s.dt.aoColumns;
            var html ='<div class="selcol1">';
            var d2 = (availableFields.length-1) /2;
            for (i=0;i<availableFields.length;i++) {
                tp = "col"+(i%2);
                if (i > d2) {
                    html += '</div><div class="selcol2">';
                    d2 = 99999999;
                }
                var selected = availableFields[i].bVisible;
                var title = availableFields[i].sTitle;
                var mData = availableFields[i].mData;
                html += '<label class="'+tp+'">'+
                        '<input name="columns" type="checkbox" checked="'+(selected ? "checked" : "")+'" value="'+mData+'">'+
                        title + '</label>';
            }
            html  += "</div>";

            return html;
        },




        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
        * Private methods (they are of course public in JS, but recommended as private)
        * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

        /**
        * Constructor logic
        *  @method  _fnConstruct
        *  @returns void
        *  @private
        */
        "_fnConstruct": function () {
            var that = this;
            var iLen = this.s.dt.aoColumns.length;
            var i;

            /* Columns discounted from reordering - counting left to right */
            if (this.s.init.iFixedColumns) {
                this.s.fixed = this.s.init.iFixedColumns;
            }

            /* Columns discounted from reordering - counting right to left */
            this.s.fixedRight = this.s.init.iFixedColumnsRight ?
         this.s.init.iFixedColumnsRight :
         0;

            /* Drop callback initialisation option */
            if (this.s.init.fnReorderCallback) {
                this.s.dropCallback = this.s.init.fnReorderCallback;
            }

            /* Allow reorder */
            if (typeof this.s.init.allowReorder != 'undefined') {
                this.s.allowReorder = this.s.init.allowReorder;
            }

            /* Allow resize */
            if (typeof this.s.init.allowResize != 'undefined') {
                this.s.allowResize = this.s.init.allowResize;
            }

            /* Allow header double click */
            if (typeof this.s.init.allowHeaderDoubleClick != 'undefined') {
                this.s.allowHeaderDoubleClick = this.s.init.allowHeaderDoubleClick;
            }

            /* Allow header contextmenu */
            if (typeof this.s.init.headerContextMenu == 'function') {
                this.s.headerContextMenu = this.s.init.headerContextMenu;
            }
            else if (this.s.init.headerContextMenu) {
                this.s.headerContextMenu = this._fnDefaultContextMenu;
            }
            else {
                this.s.headerContextMenu = false;
            }

            if (typeof this.s.init.minResizeWidth != 'undefined') {
                this.s.minResizeWidth = this.s.init.minResizeWidth;
            }

            if (typeof this.s.init.bResizeTable != 'undefined') {
                this.s.bResizeTable = this.s.init.bResizeTable;
            }

            if (typeof this.s.init.bAddFixed != 'undefined') {
                this.s.bAddFixed = this.s.init.bAddFixed;
            }

            if (typeof this.s.init.fnResizeTableCallback == 'function') {
                this.s.fnResizeTableCallback = this.s.init.fnResizeTableCallback;
            }

            /* Add event handlers for the drag and drop, and also mark the original column order */
            for (i = 0; i < iLen; i++) {
                if (i > this.s.fixed - 1 && i < iLen - this.s.fixedRight) {
                    this._fnMouseListener(i, this.s.dt.aoColumns[i].nTh);
                }

                /* Mark the original column order for later reference */
                this.s.dt.aoColumns[i]._ColReorder_iOrigCol = i;
            }

            /* State saving */
            this.s.dt.oApi._fnCallbackReg(this.s.dt, 'aoStateSaveParams', function (oS, oData) {
                that._fnStateSave.call(that, oData);
            }, "ColReorder_State");

            /* An initial column order has been specified */
            var aiOrder = null;
            if (this.s.init.aiOrder) {
                aiOrder = this.s.init.aiOrder.slice();
            }

            /* State loading, overrides the column order given */
            if (this.s.dt.oLoadedState && typeof this.s.dt.oLoadedState.ColReorder != 'undefined' &&
        this.s.dt.oLoadedState.ColReorder.length == this.s.dt.aoColumns.length) {
                aiOrder = this.s.dt.oLoadedState.ColReorder;
            }

            /* Load Column Sizes */
            var asSizes = null;
            if (this.s.dt.oLoadedState && typeof this.s.dt.oLoadedState.ColSizes != 'undefined' &&
        this.s.dt.oLoadedState.ColSizes.length == this.s.dt.aoColumns.length) {
                asSizes = this.s.dt.oLoadedState.ColSizes;
            }

            if (asSizes) {
                // Apply the sizes to the column sWidth settings
                for (i = 0, iLen = this.s.dt.aoColumns.length; i < iLen; i++)
                    this.s.dt.aoColumns[i].sWidth = asSizes[i];
            }

            /* If we have an order and/or sizing to apply - do so */
            if (aiOrder || asSizes) {
                /* We might be called during or after the DataTables initialisation. If before, then we need
                * to wait until the draw is done, if after, then do what we need to do right away
                */
                if (!that.s.dt._bInitComplete) {
                    var bDone = false;
                    this.s.dt.aoDrawCallback.push({
                        "fn": function () {
                            if (!that.s.dt._bInitComplete && !bDone) {
                                bDone = true;
                                if (aiOrder) {
                                    var resort = fnInvertKeyValues(aiOrder);
                                    that._fnOrderColumns.call(that, resort);
                                }
                                if (asSizes)
                                    that._fnResizeColumns.call(that);
                            }
                        },
                        "sName": "ColReorder_Pre"
                    });
                }
                else {
                    if (aiOrder) {
                        var resort = fnInvertKeyValues(aiOrder);
                        that._fnOrderColumns.call(that, resort);
                    }
                    if (asSizes)
                        that._fnResizeColumns.call(that);
                }
            }
        },

        /**
        * Default Context menu to display the column selectors
        *  @method  _fnDefaultContextMenu
        *  @param   Object e Event object of the contextmenu (right click) event
        *  @param   Object settings The datatables settings object
        *  @param   Object ColReorderObj The ColReorder object  
        *  @returns void
        *  @private 
        */
        "_fnDefaultContextMenu" : function(e,settings,thatObj) {
                var colSelects = thatObj.fnGetColumnSelectList();
                var myelm = $('<div></div>');
                myelm.append(colSelects);
                $("input",myelm).off("change").on("change", function(e) {
                    var index = $('input',myelm).index($(this));
                    var checked = $(this).is(":checked");
                    settings.oInstance.fnSetColumnVis(index,checked,true);
                });
                
            if (jQuery.ui) {
                myelm.dialog({
                    "position":[e.clientX,e.clientY],
                    "title":"Select Columns",
                    "modal":true,
                    "autoOpen":true,
                    "close":function(event,ui) {
                        myelm.remove();
                    }
                });
            }
            else {
                var overlay = $('<div class="overlayDiv"></div>').appendTo("body").css({"position":"fixed",top:0,left:0, width:"100%",height:"100%","z-index":5000});
                myelm.appendTo("body").css({position:"absolute", top:e.clientY-2, "background-color":"grey", left:e.clientX-2, "z-index":5005, "border":"1px solid black"});
                var timer = 0;
                myelm.mouseover(function(e) {
                    if (timer) {
                        clearTimeout(timer);
                    }
                });

                myelm.mouseout(function(e) {
                    if (timer) {
                        clearTimeout(timer);
                    }
                    timer = setTimeout(
                        function() {
                        overlay.remove();
                        myelm.remove();
                    },200);
                });
            }

        },

        /**
        * Set the column sizes (widths) from an array
        *  @method  _fnResizeColumns
        *  @returns void
        *  @private 
        */
        "_fnResizeColumns": function () {
            for (var i = 0, iLen = this.s.dt.aoColumns.length; i < iLen; i++) {
                if (this.s.dt.aoColumns[i].sWidth)
                    this.s.dt.aoColumns[i].nTh.style.width = this.s.dt.aoColumns[i].sWidth;
            }

            /* Save the state */
            // this.s.dt.oInstance.oApi._fnSaveState(this.s.dt);
        },

        /**
        * Set the column order from an array
        *  @method  _fnOrderColumns
        *  @param   array a An array of integers which dictate the column order that should be applied
        *  @returns void
        *  @private
        */
        "_fnOrderColumns": function (a) {
            if (a.length != this.s.dt.aoColumns.length) {
                this.s.dt.oInstance.oApi._fnLog(this.s.dt, 1, "ColReorder - array reorder does not " +
            "match known number of columns. Skipping.");
                return;
            }

            for (var i = 0, iLen = a.length; i < iLen; i++) {
                var currIndex = $.inArray(i, a);
                if (i != currIndex) {
                    /* Reorder our switching array */
                    fnArraySwitch(a, currIndex, i);

                    /* Do the column reorder in the table */
                    this.s.dt.oInstance.fnColReorder(currIndex, i);
                }
            }

            /* When scrolling we need to recalculate the column sizes to allow for the shift */
            if (this.s.dt.oScroll.sX !== "" || this.s.dt.oScroll.sY !== "") {
                this.s.dt.oInstance.fnAdjustColumnSizing();
            }

            /* Save the state */
            this.s.dt.oInstance.oApi._fnSaveState(this.s.dt);
        },


        /**
        * Because we change the indexes of columns in the table, relative to their starting point
        * we need to reorder the state columns to what they are at the starting point so we can
        * then rearrange them again on state load!
        *  @method  _fnStateSave
        *  @param   object oState DataTables state
        *  @returns string JSON encoded cookie string for DataTables
        *  @private
        */
        "_fnStateSave": function (oState) {
            var i, iLen, aCopy, iOrigColumn;
            var oSettings = this.s.dt;

            /* Sorting */
            for (i = 0; i < oState.aaSorting.length; i++) {
                oState.aaSorting[i][0] = oSettings.aoColumns[oState.aaSorting[i][0]]._ColReorder_iOrigCol;
            }

            var aSearchCopy = $.extend(true, [], oState.aoSearchCols);
            oState.ColReorder = [];
            oState.ColSizes = [];

            for (i = 0, iLen = oSettings.aoColumns.length; i < iLen; i++) {
                iOrigColumn = oSettings.aoColumns[i]._ColReorder_iOrigCol;

                /* Column filter */
                oState.aoSearchCols[iOrigColumn] = aSearchCopy[i];

                /* Visibility */
                oState.abVisCols[iOrigColumn] = oSettings.aoColumns[i].bVisible;

                /* Column reordering */
                oState.ColReorder.push(iOrigColumn);

                /* Column Sizes */
                oState.ColSizes[iOrigColumn] = oSettings.aoColumns[i].sWidth;
            }
        },


        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
        * Mouse drop and drag
        */

        /**
        * Add a mouse down listener to a particluar TH element
        *  @method  _fnMouseListener
        *  @param   int i Column index
        *  @param   element nTh TH element clicked on
        *  @returns void
        *  @private
        */
        "_fnMouseListener": function (i, nTh) {
            var that = this;

            var thead = $(nTh).closest('thead');
            // listen to mousemove event for resize
            if (this.s.allowResize) {
                //$(nTh).bind('mousemove.ColReorder', function (e) {
                thead.bind('mousemove.ColReorder', function (e) {
                    var nTable = that.s.dt.nTable;
                    if (that.dom.drag === null && that.dom.resize === null) {
                        /* Store information about the mouse position */
                        var nThTarget = e.target.nodeName == "TH" ? e.target : $(e.target).parents('TH')[0];
                        var offset = $(nThTarget).offset();
                        var nLength = $(nThTarget).innerWidth();

                        /* are we on the col border (if so, resize col) */
                        if (Math.abs(e.pageX - Math.round(offset.left + nLength)) <= 5) {
                            $(nThTarget).css({ 'cursor': 'col-resize' });
                            // catch gaps between cells
                            //$(nTable).css({'cursor' : 'col-resize'});
                            that.dom.resizeCol = "right";
                        }
                        else if ((e.pageX - offset.left) < 5) {
                            $(nThTarget).css({'cursor' : 'col-resize'});
                            //$(nTable).css({'cursor' : 'col-resize'});
                            that.dom.resizeCol = "left";
                        }
                        else {
                            $(nThTarget).css({ 'cursor': 'pointer' });
                            //$(nTable).css({'cursor' : 'pointer'});
                        }
                    }
                });


            }

            $(nTh).on('mousedown.ColReorder', function (e) {
                e.preventDefault();
                that._fnMouseDown.call(that, e, nTh, i);
            });

            // Add doubleclick also
            // It is best to disable sorting if using double click 
            if (this.s.allowHeaderDoubleClick) {
                $(nTh).on("dblclick.ColReorder", function(e) {
                    e.preventDefault();
                    that._fnDblClick.call(that, e, nTh, i);
                });
            }

            if (this.s.headerContextMenu) {
                $(nTh).off("contextmenu.ColReorder").on("contextmenu.ColReorder", function(e) {
                    e.preventDefault();
                    that.s.headerContextMenu.call(this,e,that.s.dt,that);
                });

            }
        },


        /**
        * Double click on a TH element in the table header
        *  @method  _fnMouseDown
        *  @param   event e Mouse event
        *  @param   element nTh TH element to be resized
        *  @returns void
        *  @private
        */
        "_fnDblClick": function (e, nTh, index) {
            var nTable = this.s.dt.nTable;
            var tableWidth = $(nTable).width();
            var aoColumns = this.s.dt.aoColumns;

            var realWidths = $.map($('th',$(this.s.dt.nThead)), function(l) {return $(l).width();});
            var nThWidth = $(nTh).width();
            var newWidth;
            var that = this;

            var tableResizeIt = function() {
                var newTableWidth = tableWidth + newWidth - nThWidth;

                $(nTable).width(newTableWidth);
                $(nTable).css('table-layout',"fixed");
                $(nTh).width(newWidth);

                aoColumns[index].sWidth = newWidth+"px";
                that.s.fnResizeTableCallback(nTable,newTableWidth,newTableWidth-tableWidth);
            };
           
            if ($(nTh).hasClass('maxwidth')) {
                var newHead = $(nTable).clone();
                $('tbody', newHead).remove();
                var newItem = $(nTh).clone();
                newItem.wrap("<tr />");
                newItem.wrap("<table />");
                $(nTable).css({'table-layout':"auto","width":"auto"});
                this.s.dt.oFeatures.bAutoWidth = true;
                // Lets try resizing to headers instead
                newWidth = this.s.minResizeWidth;
                $(nTh).removeClass('maxwidth');
            }

            else {
                $(nTable).css({'table-layout':"auto","width":"auto"});
                newWidth = $('th',nTable).eq(index).width();

                $(nTh).addClass("maxwidth");
                tableResizeIt();
            }



        },


        /**
        * Mouse down on a TH element in the table header
        *  @method  _fnMouseDown
        *  @param   event e Mouse event
        *  @param   element nTh TH element to be dragged
        *  @returns void
        *  @private
        */
        "_fnMouseDown": function (e, nTh, i) {
            var that = this;
            var target, offset, idx, nThNext, nThPrev;
            /* are we resizing a column ? */
            if ($(nTh).css('cursor') == 'col-resize') {
                // are we at the right or left?
                this.s.mouse.startX = e.pageX;
                this.s.tableWidth = $(nTh).closest("table").width();


                // If we are at the left end, we expand the previous column
                if (this.dom.resizeCol == "left") {
                    nThPrev = $(nTh).prev();
                    this.s.mouse.startWidth = $(nThPrev).width();
                    this.s.mouse.resizeElem = $(nThPrev);
                    nThNext = $(nTh).next();
                    this.s.mouse.nextStartWidth = $(nTh).width();
                    this.s.mouse.targetIndex = $('th', nTh.parentNode).index(nThPrev);
                    this.s.mouse.fromIndex = this.s.dt.oInstance.oApi._fnVisibleToColumnIndex(this.s.dt, this.s.mouse.targetIndex);
                }

                // If we are at the right end of column, we expand the current column
                else {
                    this.s.mouse.startWidth = $(nTh).width();
                    this.s.mouse.resizeElem = $(nTh);
                    nThNext = $(nTh).next();
                    this.s.mouse.nextStartWidth = $(nThNext).width();
                    this.s.mouse.targetIndex = $('th', nTh.parentNode).index(nTh);
                    this.s.mouse.fromIndex = this.s.dt.oInstance.oApi._fnVisibleToColumnIndex(this.s.dt, this.s.mouse.targetIndex);
                }

                that.dom.resize = true;
                ////////////////////
                //Martin Marchetta 
                //a. Disable column sorting so as to avoid issues when finishing column resizing
                target = $(e.target).closest('th, td');
                offset = target.offset();
                idx = $.inArray(target[0], $.map(this.s.dt.aoColumns, function (o) { return o.nTh; }));
                // store state so we don't tunr on sorting where we don't want it
                this.s.dt.aoColumns[idx]._oldbSortable = this.s.dt.aoColumns[idx].bSortable;
                this.s.dt.aoColumns[idx].bSortable = false;
                //b. Disable Autowidth feature (now the user is in charge of setting column width so keeping this enabled looses changes after operations)
                this.s.dt.oFeatures.bAutoWidth = false;
                ////////////////////
            }
            else if (this.s.allowReorder) {
                that.dom.resize = null;
                /* Store information about the mouse position */
                target = $(e.target).closest('th, td');
                offset = target.offset();
                idx = $.inArray(target[0], $.map(this.s.dt.aoColumns, function (o) { return o.nTh; }));

                if (idx === -1) {
                    return;
                }

                this.s.mouse.startX = e.pageX;
                this.s.mouse.startY = e.pageY;
                this.s.mouse.offsetX = e.pageX - offset.left;
                this.s.mouse.offsetY = e.pageY - offset.top;
                this.s.mouse.target = target[0];
                this.s.mouse.targetIndex = idx;
                this.s.mouse.fromIndex = idx;

                this._fnRegions();
            }
            /* Add event handlers to the document */
            $(document).on('mousemove.ColReorder', function (e) {
                that._fnMouseMove.call(that, e, i);
            }).on('mouseup.ColReorder', function (e) {
                setTimeout(function () {
                    that._fnMouseUp.call(that, e, i);
                }, 10);
            });
        },


        /**
        * Deal with a mouse move event while dragging a node
        *  @method  _fnMouseMove
        *  @param   event e Mouse event
        *  @returns void
        *  @private
        */
        "_fnMouseMove": function (e) {
            var that = this;
            ////////////////////
            //Martin Marchetta: Determine if ScrollX is enabled
            var scrollXEnabled;

            scrollXEnabled = this.s.dt.oInit.sScrollX === "" ? false : true;

            //Keep the current table's width (used in case sScrollX is enabled to resize the whole table, giving an Excel-like behavior)
            if (this.table_size < 0 && scrollXEnabled && $('div.dataTables_scrollHead', this.s.dt.nTableWrapper) !== undefined) {
                if ($('div.dataTables_scrollHead', this.s.dt.nTableWrapper).length > 0)
                    this.table_size = $($('div.dataTables_scrollHead', this.s.dt.nTableWrapper)[0].childNodes[0].childNodes[0]).width();
            }
            ////////////////////

            /* are we resizing a column ? */
            if (this.dom.resize) {
                var nTh = this.s.mouse.resizeElem;
                var nThNext = $(nTh).next();
                var moveLength = e.pageX - this.s.mouse.startX;
                var newWidth = this.s.mouse.startWidth + moveLength;
                // set a min width of 10 - this would be good to configure
                if (newWidth < this.s.minResizeWidth) {
                    newWidth = this.s.minResizeWidth;
                    moveLength = newWidth - this.s.mouse.startWidth ;
                }
                if (moveLength !== 0 && !scrollXEnabled) {
                    $(nThNext).width(this.s.mouse.nextStartWidth - moveLength);
                }
                $(nTh).width(this.s.mouse.startWidth + moveLength);

                //Martin Marchetta: Resize the header too (if sScrollX is enabled)
                if (scrollXEnabled && $('div.dataTables_scrollHead', this.s.dt.nTableWrapper).length) {
                    if ($('div.dataTables_scrollHead', this.s.dt.nTableWrapper).length > 0)
                        $($('div.dataTables_scrollHead', this.s.dt.nTableWrapper)[0].childNodes[0].childNodes[0]).width(this.table_size + moveLength);
                }

                ////////////////////////
                //Martin Marchetta: Fixed col resizing when the scroller is enabled.
                var visibleColumnIndex;
                //First determine if this plugin is being used along with the smart scroller...
                if ($('div.dataTables_scrollBody').lenggthll) {
                    //...if so, when resizing the header, also resize the table's body (when enabling the Scroller, the table's header and
                    //body are split into different tables, so the column resizing doesn't work anymore)
                    if ($('div.dataTables_scrollBody').length > 0) {
                        //Since some columns might have been hidden, find the correct one to resize in the table's body
                        var currentColumnIndex;
                        visibleColumnIndex = -1;
                        for (currentColumnIndex = -1; currentColumnIndex < this.s.dt.aoColumns.length - 1 && currentColumnIndex != colResized; currentColumnIndex++) {
                            if (this.s.dt.aoColumns[currentColumnIndex + 1].bVisible)
                                visibleColumnIndex++;
                        }

                        //Get the scroller's div
                        tableScroller = $('div.dataTables_scrollBody', this.s.dt.nTableWrapper)[0];

                        //Get the table
                        scrollingTableHead = $(tableScroller)[0].childNodes[0].childNodes[0].childNodes[0];

                        //Resize the columns
                        if (moveLength  && !scrollXEnabled) {
                            $($(scrollingTableHead)[0].childNodes[visibleColumnIndex + 1]).width(this.s.mouse.nextStartWidth - moveLength);
                        }
                        $($(scrollingTableHead)[0].childNodes[visibleColumnIndex]).width(this.s.mouse.startWidth + moveLength);

                        //Resize the table too
                        if (scrollXEnabled) {
                            $($(tableScroller)[0].childNodes[0]).width(this.table_size + moveLength);
                        }
                    }
                }

                if (this.s.bResizeTable) {
                    var tableMove = this.s.tableWidth + moveLength;
                    $(nTh).closest('table').width(tableMove);
                    this.s.fnResizeTableCallback($(nTh).closest('table'),tableMove,moveLength);
                }

                ////////////////////////

                return;
            }
            else if (this.s.allowReorder) {
                if (this.dom.drag === null) {
                    /* Only create the drag element if the mouse has moved a specific distance from the start
                    * point - this allows the user to make small mouse movements when sorting and not have a
                    * possibly confusing drag element showing up
                    */
                    if (Math.pow(
            Math.pow(e.pageX - this.s.mouse.startX, 2) +
            Math.pow(e.pageY - this.s.mouse.startY, 2), 0.5) < 5) {
                        return;
                    }
                    this._fnCreateDragNode();
                }

                /* Position the element - we respect where in the element the click occured */
                this.dom.drag.css({
                    left: e.pageX - this.s.mouse.offsetX,
                    top: e.pageY - this.s.mouse.offsetY
                });

                /* Based on the current mouse position, calculate where the insert should go */
                var bSet = false;
                var lastToIndex = this.s.mouse.toIndex;

                for (var i = 1, iLen = this.s.aoTargets.length; i < iLen; i++) {
                    if (e.pageX < this.s.aoTargets[i - 1].x + ((this.s.aoTargets[i].x - this.s.aoTargets[i - 1].x) / 2)) {
                        this.dom.pointer.css('left', this.s.aoTargets[i - 1].x);
                        this.s.mouse.toIndex = this.s.aoTargets[i - 1].to;
                        bSet = true;
                        break;
                    }
                }

                // The insert element wasn't positioned in the array (less than
                // operator), so we put it at the end
                if (!bSet) {
                    this.dom.pointer.css('left', this.s.aoTargets[this.s.aoTargets.length - 1].x);
                    this.s.mouse.toIndex = this.s.aoTargets[this.s.aoTargets.length - 1].to;
                }

                // Perform reordering if realtime updating is on and the column has moved
                if (this.s.init.bRealtime && lastToIndex !== this.s.mouse.toIndex) {
                    this.s.dt.oInstance.fnColReorder(this.s.mouse.fromIndex, this.s.mouse.toIndex);
                    this.s.mouse.fromIndex = this.s.mouse.toIndex;
                    this._fnRegions();
                }
            }
        },


        /**
        * Finish off the mouse drag and insert the column where needed
        *  @method  _fnMouseUp
        *  @param   event e Mouse event
        *  @returns void
        *  @private
        */
        "_fnMouseUp": function (e, colResized) {
            var that = this;

            $(document).off('mousemove.ColReorder mouseup.ColReorder');

            if (this.dom.drag !== null) {
                /* Remove the guide elements */
                this.dom.drag.remove();
                this.dom.pointer.remove();
                this.dom.drag = null;
                this.dom.pointer = null;

                /* Actually do the reorder */
                this.s.dt.oInstance.fnColReorder(this.s.mouse.fromIndex, this.s.mouse.toIndex);

                /* When scrolling we need to recalculate the column sizes to allow for the shift */
                if (this.s.dt.oScroll.sX !== "" || this.s.dt.oScroll.sY !== "") {
                    this.s.dt.oInstance.fnAdjustColumnSizing();
                }

                if (this.s.dropCallback !== null) {
                    this.s.dropCallback.call(this);
                }

                /* Save the state */
                this.s.dt.oInstance.oApi._fnSaveState(this.s.dt);
            }
            else if (this.dom.resize !== null) {
                var i;
                var j;
                var column;
                var currentColumn;
                var aoColumnsColumnindex;
                var nextVisibleColumnIndex;
                var previousVisibleColumnIndex;
                var scrollXEnabled;
                var resizeCol = this.dom.resizeCol;
/*
                if (resizeCol == 'right') {
                    colResized++;
                }
*/
                for (i = 0; i < this.s.dt.aoColumns.length; i++) {
                    if (this.s.dt.aoColumns[i]._ColReorder_iOrigCol === colResized) {
                        aoColumnsColumnindex = i;
                        break;
                    }
                }

                // Re-enable column sorting
                // only if sorting were previously enabled
                this.s.dt.aoColumns[aoColumnsColumnindex].bSortable = this.s.dt.aoColumns[aoColumnsColumnindex]._oldbSortable;

                // Save the new resized column's width
                this.s.dt.aoColumns[aoColumnsColumnindex].sWidth = $(this.s.mouse.resizeElem).innerWidth() + "px";

                // If other columns might have changed their size, save their size too
                scrollXEnabled = this.s.dt.oInit.sScrollX === "" ? false : true;
                if (!scrollXEnabled) {
                    //The colResized index (internal model) here might not match the visible index since some columns might have been hidden
                    for (nextVisibleColumnIndex = colResized + 1; nextVisibleColumnIndex < this.s.dt.aoColumns.length; nextVisibleColumnIndex++) {
                        if (this.s.dt.aoColumns[nextVisibleColumnIndex].bVisible)
                            break;
                    }

                    for (previousVisibleColumnIndex = colResized - 1; previousVisibleColumnIndex >= 0; previousVisibleColumnIndex--) {
                        if (this.s.dt.aoColumns[previousVisibleColumnIndex].bVisible)
                            break;
                    }

                    if (this.s.dt.aoColumns.length > nextVisibleColumnIndex)
                        this.s.dt.aoColumns[nextVisibleColumnIndex].sWidth = $(this.s.mouse.resizeElem).next().innerWidth() + "px";
                    else { //The column resized is the right-most, so save the sizes of all the columns at the left
                        currentColumn = this.s.mouse.resizeElem;
                        for (i = previousVisibleColumnIndex; i > 0; i--) {
                            if (this.s.dt.aoColumns[i].bVisible) {
                                currentColumn = $(currentColumn).prev();
                                this.s.dt.aoColumns[i].sWidth = $(currentColumn).innerWidth() + "px";
                            }
                        }
                    }
                }

                //Update the internal storage of the table's width (in case we changed it because the user resized some column and scrollX was enabled
                if (scrollXEnabled && $('div.dataTables_scrollHead', this.s.dt.nTableWrapper).length) {
                    if ($('div.dataTables_scrollHead', this.s.dt.nTableWrapper).length > 0) {
                        this.table_size = $($('div.dataTables_scrollHead', this.s.dt.nTableWrapper)[0].childNodes[0].childNodes[0]).width();
                    }
                }

                $(this.s.dt.nTableWrapper).width($(this.s.dt.nTable).width());

                //Save the state
                this.s.dt.oInstance.oApi._fnSaveState(this.s.dt);
            }
            ///////////////////////////////////////////////////////

            this.dom.resize = null;
        },


        /**
        * Calculate a cached array with the points of the column inserts, and the
        * 'to' points
        *  @method  _fnRegions
        *  @returns void
        *  @private
        */
        "_fnRegions": function () {
            var aoColumns = this.s.dt.aoColumns;

            this.s.aoTargets.splice(0, this.s.aoTargets.length);

            this.s.aoTargets.push({
                "x": $(this.s.dt.nTable).offset().left,
                "to": 0
            });

            var iToPoint = 0;
            for (var i = 0, iLen = aoColumns.length; i < iLen; i++) {
                /* For the column / header in question, we want it's position to remain the same if the
                * position is just to it's immediate left or right, so we only incremement the counter for
                * other columns
                */
                if (i != this.s.mouse.fromIndex) {
                    iToPoint++;
                }

                if (aoColumns[i].bVisible) {
                    this.s.aoTargets.push({
                        "x": $(aoColumns[i].nTh).offset().left + $(aoColumns[i].nTh).outerWidth(),
                        "to": iToPoint
                    });
                }
            }

            /* Disallow columns for being reordered by drag and drop, counting right to left */
            if (this.s.fixedRight !== 0) {
                this.s.aoTargets.splice(this.s.aoTargets.length - this.s.fixedRight);
            }

            /* Disallow columns for being reordered by drag and drop, counting left to right */
            if (this.s.fixed !== 0) {
                this.s.aoTargets.splice(0, this.s.fixed);
            }
        },


        /**
        * Copy the TH element that is being drags so the user has the idea that they are actually
        * moving it around the page.
        *  @method  _fnCreateDragNode
        *  @returns void
        *  @private
        */
        "_fnCreateDragNode": function () {
            var scrolling = this.s.dt.oScroll.sX !== "" || this.s.dt.oScroll.sY !== "";

            var origCell = this.s.dt.aoColumns[this.s.mouse.targetIndex].nTh;
            var origTr = origCell.parentNode;
            var origThead = origTr.parentNode;
            var origTable = origThead.parentNode;
            var cloneCell = $(origCell).clone();

            // This is a slightly odd combination of jQuery and DOM, but it is the
            // fastest and least resource intensive way I could think of cloning
            // the table with just a single header cell in it.
            this.dom.drag = $(origTable.cloneNode(false))
         .addClass('DTCR_clonedTable')
         .append(
            origThead.cloneNode(false).appendChild(
               origTr.cloneNode(false).appendChild(
                  cloneCell[0]
               )
            )
         )
         .css({
             position: 'absolute',
             top: 0,
             left: 0,
             width: $(origCell).outerWidth(),
             height: $(origCell).outerHeight()
         })
         .appendTo('body');

            this.dom.pointer = $('<div></div>')
         .addClass('DTCR_pointer')
         .css({
             position: 'absolute',
             top: scrolling ?
               $('div.dataTables_scroll', this.s.dt.nTableWrapper).offset().top :
               $(this.s.dt.nTable).offset().top,
             height: scrolling ?
               $('div.dataTables_scroll', this.s.dt.nTableWrapper).height() :
               $(this.s.dt.nTable).height()
         })
         .appendTo('body');
        },

        /**
        * Clean up ColReorder memory references and event handlers
        *  @method  _fnDestroy
        *  @returns void
        *  @private
        */
        "_fnDestroy": function () {
            var i, iLen;

            for (i = 0, iLen = this.s.dt.aoDrawCallback.length; i < iLen; i++) {
                if (this.s.dt.aoDrawCallback[i].sName === 'ColReorder_Pre') {
                    this.s.dt.aoDrawCallback.splice(i, 1);
                    break;
                }
            }

            for (i = 0, iLen = ColReorder.aoInstances.length; i < iLen; i++) {
                if (ColReorder.aoInstances[i] === this) {
                    ColReorder.aoInstances.splice(i, 1);
                    break;
                }
            }

            $(this.s.dt.nTHead).find('*').off('.ColReorder');

            this.s.dt.oInstance._oPluginColReorder = null;
            this.s = null;
        }
    };





    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    * Static parameters
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
    * Array of all ColReorder instances for later reference
    *  @property ColReorder.aoInstances
    *  @type     array
    *  @default  []
    *  @static
    *  @private
    */
    ColReorder.aoInstances = [];


    /**
    * ColReorder default settings for initialisation
    *  @namespace
    *  @static
    */
    ColReorder.defaults = {
        /**
        * Predefined ordering for the columns that will be applied automatically
        * on initialisation. If not specified then the order that the columns are
        * found to be in the HTML is the order used.
        *  @type array
        *  @default null
        *  @static
        *  @example
        *      // Using the `oColReorder` option in the DataTables options object
        *      $('#example').dataTable( {
        *          "sDom": 'Rlfrtip',
        *          "oColReorder": {
        *              "aiOrder": [ 4, 3, 2, 1, 0 ]
        *          }
        *      } );
        *
        *  @example
        *      // Using `new` constructor
        *      $('#example').dataTable()
        *
        *      new $.fn.dataTable.ColReorder( '#example', {
        *          "aiOrder": [ 4, 3, 2, 1, 0 ]
        *      } );
        */
        aiOrder: null,

        /**
        * Redraw the table's column ordering as the end user draws the column
        * (`true`) or wait until the mouse is released (`false` - default). Note
        * that this will perform a redraw on each reordering, which involves an
        * Ajax request each time if you are using server-side processing in
        * DataTables.
        *  @type boolean
        *  @default false
        *  @static
        *  @example
        *      // Using the `oColReorder` option in the DataTables options object
        *      $('#example').dataTable( {
        *          "sDom": 'Rlfrtip',
        *          "oColReorder": {
        *              "bRealtime": true
        *          }
        *      } );
        *
        *  @example
        *      // Using `new` constructor
        *      $('#example').dataTable()
        *
        *      new $.fn.dataTable.ColReorder( '#example', {
        *          "bRealtime": true
        *      } );
        */
        bRealtime: false,

        /**
        * Indicate how many columns should be fixed in position (counting from the
        * left). This will typically be 1 if used, but can be as high as you like.
        *  @type int
        *  @default 0
        *  @static
        *  @example
        *      // Using the `oColReorder` option in the DataTables options object
        *      $('#example').dataTable( {
        *          "sDom": 'Rlfrtip',
        *          "oColReorder": {
        *              "iFixedColumns": 1
        *          }
        *      } );
        *
        *  @example
        *      // Using `new` constructor
        *      $('#example').dataTable()
        *
        *      new $.fn.dataTable.ColReorder( '#example', {
        *          "iFixedColumns": 1
        *      } );
        */
        iFixedColumns: 0,

        /**
        * As `iFixedColumnsRight` but counting from the right.
        *  @type int
        *  @default 0
        *  @static
        *  @example
        *      // Using the `oColReorder` option in the DataTables options object
        *      $('#example').dataTable( {
        *          "sDom": 'Rlfrtip',
        *          "oColReorder": {
        *              "iFixedColumnsRight": 1
        *          }
        *      } );
        *
        *  @example
        *      // Using `new` constructor
        *      $('#example').dataTable()
        *
        *      new $.fn.dataTable.ColReorder( '#example', {
        *          "iFixedColumnsRight": 1
        *      } );
        */
        iFixedColumnsRight: 0,

        /**
        * Callback function that is fired when columns are reordered
        *  @type function():void
        *  @default null
        *  @static
        *  @example
        *      // Using the `oColReorder` option in the DataTables options object
        *      $('#example').dataTable( {
        *          "sDom": 'Rlfrtip',
        *          "oColReorder": {
        *              "fnReorderCallback": function () {
        *                  alert( 'Columns reordered' );
        *              }
        *          }
        *      } );
        *
        *  @example
        *      // Using `new` constructor
        *      $('#example').dataTable()
        *
        *      new $.fn.dataTable.ColReorder( '#example', {
        *          "fnReorderCallback": function () {
        *              alert( 'Columns reordered' );
        *          }
        *      } );
        */
        fnReorderCallback: null
    };





    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    * Static functions
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
    * `Deprecated` Reset the column ordering for a DataTables instance
    *  @method  ColReorder.fnReset
    *  @param   object oTable DataTables instance to consider
    *  @returns void
    *  @static
    *  @deprecated Use `ColReorder( table ).fnReset()` instead.
    */
    ColReorder.fnReset = function (oTable) {
        for (var i = 0, iLen = ColReorder.aoInstances.length; i < iLen; i++) {
            if (ColReorder.aoInstances[i].s.dt.oInstance == oTable) {
                ColReorder.aoInstances[i].fnReset();
            }
        }
    };






    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    * Constants
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
    * ColReorder version
    *  @constant  VERSION
    *  @type      String
    *  @default   As code
    */
    ColReorder.VERSION = "1.1.0-dev";
    ColReorder.prototype.VERSION = ColReorder.VERSION;





    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    * Initialisation
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /*
    * Register a new feature with DataTables
    */
    if (typeof $.fn.dataTable == "function" &&
     typeof $.fn.dataTableExt.fnVersionCheck == "function" &&
     $.fn.dataTableExt.fnVersionCheck('1.9.3')) {
        $.fn.dataTableExt.aoFeatures.push({
            "fnInit": function (settings) {
                var table = settings.oInstance;

                if (table._oPluginColReorder === undefined) {
                    var opts = settings.oInit.oColReorder !== undefined ?
                       settings.oInit.oColReorder :
                       {};

                    table._oPluginColReorder = new ColReorder(settings, opts);
                }
                else {
                    table.oApi._fnLog(settings, 1, "ColReorder attempted to initialise twice. Ignoring second");
                }

                return null; /* No node for DataTables to insert */
            },
            "cFeature": "R",
            "sFeature": "ColReorder"
        });
    }
    else {
        alert("Warning: ColReorder requires DataTables 1.9.3 or greater - www.datatables.net/download");
    }


    window.ColReorder = ColReorder;
    $.fn.dataTable.ColReorder = ColReorder;


})(jQuery, window, document);


