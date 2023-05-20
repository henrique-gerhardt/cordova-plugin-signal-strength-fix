function SignalStrength() {
  this.dbm = function(callback) {
    return cordova.exec(callback, function(err) {
      callback(-1);
    }, "SignalStrength", "dbm", []);

  };
  this.asu = function(callback) {
    return cordova.exec(callback, function(err) {
      callback(-1);
    }, "SignalStrength", "asu", []);

  };
  this.type = function(callback) {
    return cordova.exec(callback, function(err) {
      callback('unknown');
    }, "SignalStrength", "type", []);

  };
}

window.SignalStrength = new SignalStrength()
