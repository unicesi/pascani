const thinky = require('../util/thinky');
const type = thinky.type;

const Value = thinky.createModel("values", {
	createdAt: type.date().default(thinky.r.now()),
	id: type.string(),
	name: type.string(),
	value: type.any(),
});

module.exports = Value;

const Variable = require('./variable');
Value.belongsTo(Variable, "variable", "variableId", "id");
