Element.implement({
  observe: function(options) {
    return new Obm.Observer(this, options); 
  } 
}); 

HideTimer = new Class({
  Implements: Options,   

  options : {
    duration: 1000,
    fn: function() {
      this.el.setStyle('display','none');
      this.el.setStyle('visibility','hidden');
      overListBoxFix(this.el,'none');
    },
    elems: new Array()
  },

  initialize: function(el,options) {
    this.setOptions(options);
    this.options.fn = this.options.fn.bind(this);
    this.el = $(el);
    this.timer = null;
    if(this.options.elems.length > 0) {
      this.options.elems.each(function (element) {
        element.addEvent('mouseenter',this.clearTimer.bind(this));
        element.addEvent('mouseleave',this.initTimer.bind(this));
      }.bind(this));
    }
    this.el.addEvent('mouseenter',this.clearTimer.bind(this));
    this.el.addEvent('mouseleave',this.initTimer.bind(this));
  },
  
  initTimer: function() {
    if(this.el.getStyle('display') != 'none') {
      if(this.timer != null ) {
        this.clearTimer();
      }
      this.timer = this.exec.bind(this).delay(this.options.duration);
    }
  },

  clearTimer: function() {
    if(this.timer != null) {
      $clear(this.timer);
      this.timer = null;
    }
  },
  
  toggleTimer: function() {
    if(this.timer == null) {
      this.initTimer();
    } else {
      this.clearTimer();
    }
  },

  
  exec: function() {
    this.options.fn.delay(1);
    this.timer = null;
  }
});

String.prototype.pad = function(l, s, t){
    return s || (s = " "), (l -= this.length) > 0 ? (s = new Array(Math.ceil(l / s.length)
        + 1).join(s)).substr(0, t = !t ? l : t == 1 ? 0 : Math.ceil(l / 2))
        + this + s.substr(0, l - t) : this;
};


Number.prototype.pad = function(l,s,t) { 
  return this.toString().pad(l,s,t);
}

Obm.CoordonateWidget = new Class({
  Implements: Options,

  kind: 'dummy',
  options: {inject: 'inside', remove: true},
  newId: function() { return 0;},

  setValues: function() {
    this.structure = $merge.run([this.structure].extend(arguments));
  },

  initialize: function(fields, options) {
    this.id = this.newId();
    this.setOptions(options);
    this.setValues(fields);
    this.table = new Element('table').addClass('coordinate').addClass(this.kind).set('id', this.kind + '-' + this.id);
    this.element = new Element('tbody');
    this.table.adopt(this.element);
    this.container = $(this.options.container);
    this.displayForm(); 
    this.table.inject(this.container, this.options.inject);
    OverText.update();
  },    

  displayForm: function() {
    for (var field in this.structure){
      var data = this.structure[field]
      if(data.newLine == true) {line =  new Element('tr'); this.element.adopt(line);}
      if(data.newCell == true || data.newLine == true) {cell = new Element('th');line.adopt(cell);}
      if(!data.newCell && !data.newLine) cell.adopt(new Element('br'));
      cell.setProperties(data.properties);
      cell.adopt(this.makeField(field, data));
      new OverText(cell.getElements('input, textarea'));      
    }
    if(this.options.remove) {
      line.adopt(new Element('td').adopt(
        new Element('a').adopt(new Element('img').setProperties({'src' : obm.vars.images.del,'alt' : obm.vars.labels.remove}))
          .addEvent('click', function() {this.table.dispose();OverText.update();}.bind(this))
          .setStyle('cursor','pointer')
        )
      );
    }
  },

  makeField: function(fieldName, field) {
    switch (field.kind) {
      case 'text' :
        var element = new Element('input').setProperties({
          'type' : 'text',
          'name' : this.kind + '[' + this.id + ']' + '[' + fieldName + ']',
          'alt' : field.label,
          'title' : field.label
        }).set('inputValue',field.value);
        break;
      case 'select' :
        var element = new Element('select').setProperties({
          'name' : this.kind + '[' + this.id + ']' + '[' + fieldName + ']',
          'alt' : field.label,
          'title' : field.label
        });
        for (var option in field.token) {
          element.adopt(new Element('option').appendText(field.token[option]).setProperty('value', option));
        }
        element.set('inputValue', field.value);
        break;
      case 'autocomplete.local' :
        var element = new Element('input').setProperties({'type' : 'text','value' : field.value});
        new Autocompleter.Local(element, field.token, {
          'minLength': 0, 
          'overflow': true,
          'selectMode': 'type-ahead',
          'multiple': field.multiple      
          });
        break;        
      case 'textarea' :
        var element = new Element('textarea').setProperties({
          'rows' : field.rows,
          'name' : this.kind + '[' + this.id + ']' + '[' + fieldName + ']',
          'alt' : field.label,
          'title' : field.label
        }).set('inputValue',field.value);
        break;        
      case 'hidden' :
        var element = new Element('input').setProperties({
          'type' : 'hidden',
          'name' : this.kind + '[' + this.id + ']' + '[' + fieldName + ']'
        }).set('inputValue',field.value);
      break;
      case 'label' :
        var element = new Element('label').set('html',field.label)
          .adopt(new Element('input').setProperties({
            'type' : 'hidden',
            'name' : this.kind + '[' + this.id + ']' + '[' + fieldName + ']'
          }).set('inputValue',field.value));
      break;
    }

    return element;
  }
});

Obm.MultipleField = new Class({
  Implements: Options,

  options : {
    min: 1,
    max: null,
    add: null,
    filter: null
  },

  initialize: function(element, selector, options) {
    this.setOptions(options);
    this.element = $(element);
    this.selector = selector
    if(!$chk(this.options.add)) this.add = this.buildAddButton();
    else this.add = this.options.add;
    this.add.addEvent('add', this.addField.bind(this));
    this.buildMultipleField();
  },

  buildRemoveButton: function(element) {
    var remove = new Element('a').set('href','#').addEvent('click', function (evt) {
      remove.getParent().dispose();
      this.removeField(element);
    }.bind(this)).adopt(new Element('img').set('src', obm.vars.images.del))
    return remove; 
  },

  buildAddButton: function() {
    var add = new Element('a').set('href','#').addEvent('click', function (evt) {
     var clone = this.last.clone();
     clone.getElements('input, select, textarea').set('inputValue','')
     this.element.adopt(clone);
     this.add.fireEvent('add'); 
    }.bind(this)).adopt(new Element('img')
                 .set('src', obm.vars.images.add));
    return add; 
  },

  buildMultipleField: function() {
    var elements = this.element.getElements(this.selector);
    this.last = elements[elements.length - 1];
    if(elements.length > this.options.min) {
      elements.each(function(child, index) {
        new Element('span').addClass('multipleFieldButtons').setStyle('float','left').injectAfter(child);
        this.buildRemoveButton(child).injectInside(child.getNext());
      }.bind(this))
    } else {
      new Element('span').addClass('multipleFieldButtons').setStyle('float','left').injectAfter(this.last); 
    }
    this.add.injectBottom(this.last.getNext());
  },

  removeField: function(element) {
    element.dispose();
    OverText.update();
    var elements = this.element.getElements(this.selector);
    if(elements.length == this.options.min) {
      this.element.getElements('.multipleFieldButtons img[src='+obm.vars.images.del+']').dispose();
    }
    this.setLastField();
  },

  addField: function() {
    var elements = this.element.getElements(this.selector);
    if(elements.length == this.options.min + 1) {
      this.buildRemoveButton(this.last).addEvent('click', this.last.dispose.bind(this.last)).injectTop(this.last.getNext());
    }
    new Element('span').addClass('multipleFieldButtons').setStyle('float','left').injectAfter(elements[elements.length - 1]);
    this.setLastField();
    this.buildRemoveButton(this.last).addEvent('click', this.last.dispose.bind(this.last)).injectTop(this.last.getNext());
  },
  
  setLastField: function() {
    var elements = this.element.getElements(this.selector);
    this.last = elements[elements.length - 1];
    this.add.dispose();
    this.add.injectBottom(this.last.getNext());
  }


});

Obm.Error = {
 
  parseStatus: function(state) {
    switch(state) {
      case 401:
        window.location.href= obm.vars.consts.obmUrl + '/';
        exit;
        break;
      default:
        break;
    }
  },

  formUpdate: function(errors, caller)  {
    errors.error.each(function( msg, field) {
      if($(field)) {
        var field = $(field);
        var title = field.get('title');
        field.addClass('error');
        field.set('title', msg);
        caller.addEvent('request', function() { field.removeClass('error'); field.set('title', title)});
      }
    });
  }

}
