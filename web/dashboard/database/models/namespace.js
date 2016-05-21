const thinky = require('../util/thinky');
const type = thinky.type;

const Namespace = thinky.createModel("namespaces", {
	createdAt: type.date().default(thinky.r.now()),
	id: type.string(),
	name: type.string(),
});

module.exports = Namespace;
