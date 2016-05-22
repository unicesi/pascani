const thinky = require('../util/thinky');
const type = thinky.type;

const Monitor = thinky.createModel("monitors", {
	createdAt: type.date().default(thinky.r.now()),
	id: type.string(),
	name: type.string(),
});

module.exports = Monitor;

const Namespace = require('./namespace');
Monitor.hasAndBelongsToMany(Namespace, "namespaces", "id", "id");
