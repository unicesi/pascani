'use strict';

const chalk = require('chalk');
const thinky = require('./util/thinky');
const models = require('./models/all');

const promises = [];
for(let name in thinky.models) {
	promises.push(thinky.models[name].ready());
}
Promise.all(promises).then(function() {
	console.log(chalk.green('✓') + ' Rethinkdb models initialized');
	process.exit(0);
});
