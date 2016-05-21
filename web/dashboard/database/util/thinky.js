const config = require('../../rethinkdb.config');
const thinky = require('thinky')({
	host: config.host,
	port: config.port,
	authKey: config.authKey,
	db: config.db,
})

module.exports = thinky;
