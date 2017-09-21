require('angular-nvd3')

module.exports = angular.module('stf.analysis',['nvd3'])
	.run(['$templateCache', function($templateCache) {
    $templateCache.put('control-panes/analysis/analysis.pug',
      require('./analysis.pug')
    )
  }])
    .controller('AnalysisCtrl', require('./analysis-controller'))
    .controller('MemoryCtrl', require('./memory-controller'))