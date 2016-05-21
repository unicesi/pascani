const thinky = require('../util/thinky');
const type = thinky.type;

const Monitor = thinky.createModel("monitors", {
	createdAt: type.date().default(thinky.r.now()),
	id: type.string(),
	name: type.string(),
});

module.exports = Monitor;

const Namepace = require('./namespace');
Monitor.hasMany(Namepace, "namespaces", "id", "monitorId");
