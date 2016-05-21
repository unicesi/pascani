const thinky = require('../util/thinky');
const type = thinky.type;

const Variable = thinky.createModel("variables", {
	createdAt: type.date().default(thinky.r.now()),
	id: type.string(),
	name: type.string(),
});

module.exports = Variable;

const Namespace = require('./namespace');
Variable.belongsTo(Namespace, "namespace", "namespaceId", "id");
