/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

//
// NOTE: extjs 3.4.1 has an Ext.define, but its no available in a GPL version, so we have to use this cruft until we move over to extjs 4.x
//

/**
 * Used as the super-class for all Ext.define classes which not not provide a class to extend.
 * Do not put anything special in here, since not everything will use this as its super-class.
 *
 * @constructor
 */
Ext.Base = function (config) {
    Ext.apply(this, config);
};

/**
 * Define a new class.
 *
 * @static
 *
 * @param {String} className        The name of the class to define.
 * @param {Object} data             Configuration for the class.
 * @param {Function} [createdFn]    Function to execute when class has been defined.
 *
 * @cfg {String} extend             Super-class name.
 * @cfg {*} statics                 Static members.
 * @cfg {*} requirejs               Array of requirejs module dependencies
 * @cfg {*} requires                Array of class names (created by Ext.define) which are dependencies.
 * @cfg {boolean} requireSuper      Flag to enable/disable automatic dependency on super-class;
 *                                  Tries to auto-detect but just inc-ase you can enable/disable it.
 * @cfg {String} xtype              Automatically register class as Ext xtype component.
 * @cfg {boolean} singleton         Defines a singleton instead of a class.
 *                                  Given class name will reference singleton object and createdFn 'this' will be the singleton object.
 */
Ext.define = function (className, data, createdFn) {
    data = data || {};

    var i,
        nameSpace,
        baseClassName,
        superName,
        type,
        requireSuper,
        superClass,
        statics,
        requiredClasses,
        requiredModules,
        moduleName,
        obj,
        arrayify,
        singleton,
        xtype,
        tmp;

    obj = function (path) {
        var context = window;
        Ext.each(path.split('.'), function (part) {
            context = context[part];
        });
        return context;
    };

    // turns input into an array of strings
    arrayify = function (input) {
        var list = [];

        if (Ext.isArray(input)) {
            for (i = 0; i < input.length; i++) {
                if (Ext.isString(input[i])) {
                    list.push(input[i]);
                }
                else {
                    throw "Invalid entry: " + input[i];
                }
            }
        }
        else if (Ext.isString(input)) {
            list.push(input);
        }
        else if (input !== undefined) {
            throw "Invalid value: " + input;
        }

        return list;
    };

    // Find the namespace (if any) for the new class
    i = className.lastIndexOf('.');
    if (i !== -1) {
        nameSpace = className.substring(0, i);
        baseClassName = className.substring(i + 1);
    }
    else {
        baseClassName = className;
    }

    requireSuper = data.requireSuper;
    delete data.requireSuper;

    // Determine the super-class
    if (data.extend !== undefined) {
        superName = data.extend;
        delete data.extend;

        // require super module if there is not already a defined class of that name for legacy support
        if (requireSuper === undefined) {
            requireSuper = obj(superName) === undefined;
        }
    }
    else {
        superName = 'Ext.Base';
        requireSuper = false;
    }

    // Extract statics
    statics = data.statics;
    delete data.statics;

    // Extract singleton
    singleton = data.singleton;
    delete data.singleton;

    // Extract xtype
    xtype = data.xtype;
    delete data.xtype;

    // Extract requirejs dependencies
    requiredModules = arrayify(data.requirejs);
    delete data.requirejs;

    // Extract class dependencies (which were defined using Ext.define)
    requiredClasses = arrayify(data.requires);
    delete data.requires;

    // Require super if enabled
    if (requireSuper === true) {
        requiredModules.push(superName.replaceAll('.', '/'));
    }

    // append translated dependency classes on to required modules
    for (i=0; i < requiredClasses.length; i++) {
        tmp = requiredClasses[i].replaceAll('.', '/');
        requiredModules.push(tmp);
    }

    // Translate class name into module name
    moduleName = className.replaceAll('.', '/');

    if (requiredModules.length !== 0) {
        Nexus.log('Defining module: ' + moduleName + ' depends: ' + requiredModules);
    }
    else {
        Nexus.log('Defining module: ' + moduleName);
    }

    define(moduleName, requiredModules, function()
    {
        Nexus.log('Defining class: ' + className + ' (ns: ' + nameSpace + ', super: ' + superName + ')');

        // Create namespace if required
        if (nameSpace) {
            Ext.namespace(nameSpace);
        }

        // Get a reference to the super-class
        superClass = obj(superName);

        // When no constructor given in configuration (its always there due to picking upt from Object.prototype), use a synthetic version
        if (data.constructor === Object.prototype.constructor) {
            data.constructor = function () {
                // Just call superclass constructor
                this.constructor.superclass.constructor.apply(this, arguments);
            };
        }

        // Create the sub-class
        type = Ext.extend(superClass, data);

        // Enrich the sub-class prototype
        Ext.apply(type.prototype, {
            // Name of defined class
            '$className': className,

             // Instance logger
            '$log': function(message) {
                Nexus.log(className + ': ' + message);
            }
        });

        // export require.js module return values as static 'modules' object
        if (requiredModules.length !== 0) {
            if (typeof statics === 'undefined') {
              statics = {};
            }
            statics.modules = {};
            for (i = 0; i < requiredModules.length; i+=1) {
                statics.modules[requiredModules[i]] = arguments[i];
            }
        }

        // Apply any static members
        if (statics !== undefined) {
            Ext.apply(type, statics);
        }

        // Register xtype for class
        if (xtype !== undefined) {
            Ext.reg(xtype, type);
        }

        // When singleton; type becomes new instance
        if (singleton !== undefined) {
            type = new type();
        }

        // Assign to global namespace
        obj(nameSpace)[baseClassName] = type;

        // Call post-define hook
        if (createdFn !== undefined) {
            // Scope to created type, empty args seems to be required here
            createdFn.call(type, []);
        }

        return type;
    });
};

// FIXME: Port over extjs-4 Ext.create() bits so we can have sane[r] object creation

// FIXME: Port over extjs-4 Ext.Error.* bits so we can have sane[r] exception handling
