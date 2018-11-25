var w = 900;
var h = 1000;
var linkDistance=200;

var randomEdgeNum = 10;
var d3gravity = 1;

// change the random edge number, gravity, linkDistance according to the difficulty
var diff = Android.getDifficulty();
switch(diff){
  case 1:
    randomEdgeNum = 10;
    break;
  case 2:
    randomEdgeNum = 20;
    break;
  case 3:
    randomEdgeNum = 30;
    break;
default:
}


var green = "#1D7874";
var red = "#DB5461";
var changeColor = green;

// to return color one by one
function colores_list(n) {
  var colores_g = ["#CFE2F3", "#8DDBE0", "#77B6EA", "#C3ACCE", "#89909F"];
  return colores_g[n % colores_g.length];
}

var toggleColor = function(i){
  var currentColor = "#ccc";
  var check;   // 1: select a edge
               // 0: cancel selecting the edge

  Android.logD("Change color to" + changeColor);

  // whether it is a correct edge
  changeColor = i<edgeData.length? green: red;
  currentColor = currentColor == "#ccc" ? changeColor: "#ccc";

  check = currentColor==="#ccc"? false:true;
  Android.changeAndCheck(i, check);

  d3.select("#edge"+i).style("stroke", currentColor);
};

function decideColor(i){
  Android.logD(i);
  changeColor = i<edgeData.length? green: red;
}

function addRandomEdge(){
  var connected=1;
  var index = -1;
  do{
    index = Math.floor(Math.random()*edgeData.length);
    connected = Android.checkConnected(index);
  }while(connected===1);

  d3.select("#edge"+index).style("stroke", green);
  Android.changeAndCheck(index, true);
}

function reset(){
  Android.init();
  d3.selectAll('line')
    .style("stroke", "#ccc");
}


var toggleColorText = (function(){
  var currentColor = "#ccc";

  return function(){
    currentColor = currentColor=="#ccc"? green: "#ccc";
    var id = this.id.substring(9, this.id.length);
    d3.select("#edge"+id).style("stroke", currentColor);
  }
})();

var colors = colores_list;


function nodeEqual(n1, n2){
  if (n1.name===n2.name){
    return true;
  }
  return false;
}

// get the node of the graph
var nodeJson = JSON.parse(Android.getNodeData());
// get the edge of the graph
var edgeJson = JSON.parse(Android.getEdgeData());
var allNodesJson = JSON.parse(Android.getAllNodeData());

// the nodes which are connected by edges
var nodeData = function(){
  return nodeJson.map(function(d){
    var labelStr = '{"name":\"'+ d.label+'\"}';
    return JSON.parse(labelStr);
  })
}();

// all the nodes of the level (some don't appear because of the difficulty
var allNodes = function(){
  return allNodesJson.map(function(d){
    var labelStr = '{"name":\"'+ d.label+'\"}';
    return JSON.parse(labelStr);
  })
}();

// get the raw edge data from edge json android
var edgeData = function(){
  var edge = edgeJson.map(function(d){
    var edgeStr = '{"source":'
                 + d.node1.substring(1, d.node1.length)
                 + ', "target": '
                 + d.node2.substring(1, d.node2.length)
                 + '}';
    return JSON.parse(edgeStr);
  });
  return edge;
}();


// change the source and target to the number in nodeData
edgeData.forEach(function(link){
  var nodeSource = allNodes[link.source];
  for(var i=0; i<nodeData.length; i++){
    if(nodeEqual(nodeData[i], nodeSource)){
      link.source = i;
      break;
    }
  }

  var nodeTarget = allNodes[link.target];
  for(var j=0;j<nodeData.length; j++){
    if(nodeEqual(nodeData[j], nodeTarget)){
      link.target = j;
      break;
    }
  }
});

// edges between every pair of nodes in nodeData
var moreEdges = function(){
  var edges = JSON.parse(JSON.stringify(edgeData));
  for(var i=0; i<randomEdgeNum; i++){
    var randomEdge = '{"source":'
                       + Math.floor(Math.random()*nodeData.length)
                       + ', "target": '
                       + Math.floor(Math.random()*nodeData.length)
                       + '}';
    edges.push(JSON.parse(randomEdge));
  }
  return edges;
}();


var svg = d3.select("body").append("svg").attr({"width":w,"height":h});

var force = d3.layout.force()
  .nodes(nodeData)
  .links(moreEdges)
  .size([w,h])
  .linkDistance([linkDistance])
  .charge([-10000])
  .theta(0.1)
  .gravity(d3gravity)
  .start();

var edges = svg.selectAll("line")
  .data(moreEdges)
  .enter()
  .append("line")
  .on("click", function(d, i){toggleColor(i);})
  .attr("id",function(d,i) {return 'edge'+i})
  .style("stroke","#ccc")
  .style("stroke-width", 8);



var nodes = svg.selectAll("circle")
  .data(nodeData)
  .enter()
  .append("circle")
  .attr({"r":20})
  .style("fill",function(d,i){return colors(i);})
  .call(force.drag)


var nodelabels = svg.selectAll(".nodelabel")
  .data(nodeData)
  .enter()
  .append("text")
  .attr({"x":function(d){return d.x;},
          "y":function(d){return d.y;},
          "class":"nodelabel",
          "font-size":12})
  .text(function(d){return d.name;});

var edgepaths = svg.selectAll(".edgepath")
  .data(moreEdges)
  .enter()
  .append('path')
  .attr({'d': function(d) {return 'M '+d.source.x+' '+d.source.y+' L '+ d.target.x +' '+d.target.y},
           'class':'edgepath',
           'fill-opacity':0,
           'stroke-opacity':0,
           'fill':'blue',
           'stroke':'red',
           'id':function(d,i) {return 'edgepath'+i}})
  .style("pointer-events", "none")
  .on("click", function(d, i){toggleColor(i);});

var edgelabels = svg.selectAll(".edgelabel")
  .data(moreEdges)
  .enter()
  .append('text')
  .attr({'class':'edgelabel',
           'id':function(d,i){return 'edgelabel'+i},
           'dx':80,
           'dy':0,
           'font-size':15,
           'fill':'black'})
  .on("click", toggleColorText);

edgelabels.append('textPath')
  .attr('xlink:href',function(d,i) {return '#edgepath'+i})
  .text(function(d, i){return ''});


svg.append('defs').append('marker')
  .attr({'id':'arrowhead',
           'viewBox':'-0 -5 10 10',
           'refX':25,
           'refY':0,
           //'markerUnits':'strokeWidth',
           'orient':'auto',
           'markerWidth':10,
           'markerHeight':10,
           'xoverflow':'visible'})
  .append('svg:path')
  .attr('d', 'M 0,-5 L 10 ,0 L 0,5')
  .attr('fill', '#ccc')
  .attr('stroke','#ccc');

force.on("tick", function(){

  edges.attr({"x1": function(d){return d.source.x;},
              "y1": function(d){return d.source.y;},
              "x2": function(d){return d.target.x;},
              "y2": function(d){return d.target.y;}
  });

  nodes.attr({"cx":function(d){return d.x;},
              "cy":function(d){return d.y;}
  });

  nodelabels.attr("x", function(d) { return d.x; })
            .attr("y", function(d) { return d.y; });

  edgepaths.attr('d', function(d) { var path='M '+d.source.x+' '+d.source.y+' L '+ d.target.x +' '+d.target.y;
                                     //console.log(d)
                                     return path});

  edgelabels.attr('transform',function(d,i){
    if (d.target.x<d.source.x){
      bbox = this.getBBox();
      rx = bbox.x+bbox.width/2;
      ry = bbox.y+bbox.height/2;
      return 'rotate(180 '+rx+' '+ry+')';
    }
    else {
      return 'rotate(0)';
    }
  });
});
