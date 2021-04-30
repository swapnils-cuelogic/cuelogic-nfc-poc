function addXMLRequestCallback(callback) {
  var oldSend, i;
  if (XMLHttpRequest.callbacks) {
    // we've already overridden send() so just add the callback
    XMLHttpRequest.callbacks.push(callback);
  } else {
    // create a callback queue
    XMLHttpRequest.callbacks = [callback];
    // store the native send()
    oldSend = XMLHttpRequest.prototype.send;
    // override the native send()
    XMLHttpRequest.prototype.send = function () {
      // call the native send()
      oldSend.apply(this, arguments);

      this.onreadystatechange = function (progress) {
        for (i = 0; i < XMLHttpRequest.callbacks.length; i++) {
          XMLHttpRequest.callbacks[i](progress);
        }
      };
    }
  }
}

addXMLRequestCallback(function (progress) {
  if (typeof progress.srcElement.responseText != 'undefined'
    && progress.srcElement.responseText != ''
    && progress.srcElement.readyState == 4) {
//    var myObj = { status : progress.srcElement.status, data : progress.srcElement.responseText };
//    console.log('Resp:: ', JSON.stringify(myObj));
    console.log('Response:: ', progress.srcElement.responseText);
    console.log('Status:: ', progress.srcElement.status);
  }
});

// Select the device input field and enter the value
var x = document.getElementById('deviceInput');
x.value = '800000534';

x.dispatchEvent(new Event('input', {
  bubbles: true
}));

// Select the employee input field and enter the value
var y = document.getElementById('employeeInput');
y.value = '12345';

y.dispatchEvent(new Event('input', {
  bubbles: true
}));

y.dispatchEvent(
  new KeyboardEvent('keydown', {
    key: 'Enter',
    bubbles: true,
    cancelable: true,
    keyCode: 13
  })
);

y.dispatchEvent(
  new KeyboardEvent('keyup', {
    key: 'Enter',
    bubbles: true,
    cancelable: true,
    keyCode: 13
  })
);