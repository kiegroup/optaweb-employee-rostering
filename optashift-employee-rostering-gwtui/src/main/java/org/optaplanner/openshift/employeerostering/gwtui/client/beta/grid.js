function scale(coordinate) {
    return Math.floor(coordinate - (coordinate % HANDLES.pixelSize()));
}

function drawGrid($viewport) {

    var $parent = $('<div />', {
        class: 'grid',
        width: $viewport.width(),
        height: $viewport.height()
    }).appendTo('.grid-container');

    for (var i = 0; i < HANDLES.gridSize($viewport); i += HANDLES.pixelSize()) {
        var $gridDiv = $('<div />');
        HANDLES.decorateGridDiv($gridDiv);
        $gridDiv.appendTo($parent);
    }
}

function toggleDraggablity($blob, $lock) {
    if ($lock.hasClass("locked")) {
        $blob.draggable('disable');
    } else {
        $blob.draggable('enable');
    }
}

function bindBlobEvents($blob) {

    $blob.draggable({
        containment: $blob.parent(), //That's the sub-lane
        axis: HANDLES.grid.orientation,
        grid: [HANDLES.grid.pixel.width, HANDLES.grid.pixel.height],
        scroll: true,
        scrollSpeed: 10,
        scrollSensitivity: 50
    });

    var $actions = $('<div />', {class: 'actions'});
    $actions.appendTo($blob);

    var $close = $('<div />', {class: 'close hide'});
    $close.appendTo($actions);
    $close.on("click", function (e) {
        $blob.remove();
        e.stopPropagation();
    });

    var $lock = $('<div />', {class: 'lock unlocked hide'});
    $lock.appendTo($actions);
    $lock.on("click", function (e) {
        $lock.toggleClass('locked');
        $lock.toggleClass('unlocked');
        e.stopPropagation();
        toggleDraggablity($blob, $lock);
    });

    toggleDraggablity($blob, $lock);

    var $resize = $('<div />', {class: 'resize hide'});
    $resize.appendTo($actions);

    $blob.on("mouseup", function (e) {

        // Remove blob (MIDDLE-CLICK)
        if (e.button === 1) {
            e.target.remove();
        }

        e.stopPropagation();
    });

    $blob.hover(function (e) {
        $blob.children(".actions").children().toggleClass("hide");
    }, function (e) {
        $blob.children(".actions").children().toggleClass("hide");
    });

    return $blob;
}

function bindSubLaneEvents($subLane) {

    $subLane.on("mouseup", function (e) {

        // Delete sub-lane (SHIFT + MIDDLE-CLICK)
        if (e.shiftKey && e.button === 1) {
            e.target.remove();
        }

        // Add sub-lane (SHIFT + CLICK)
        else if (e.shiftKey) {
            addSubLaneTo($(e.target).parent());
        }

        // Add blob (CLICK)
        else {
            addBlobTo($(e.target), {x: e.offsetX, y: e.offsetY});
        }

        e.stopPropagation();
    });
}

function addSubLaneTo($target) {
    var $subLane = $('<div />', {class: 'sub-lane'});
    $subLane.appendTo($target);
    bindSubLaneEvents($subLane);
}

function addBlobTo($target, point) {
    var $blob = $('<div />', {class: 'blob'});
    $blob.appendTo($target);
    HANDLES.decorateNewBlob($blob, point);
    bindBlobEvents($blob);
}

$(".sub-lane").each(function () {
    bindSubLaneEvents($(this));
});

$(".blob").each(function () {
    bindBlobEvents($(this));
});

drawGrid($('.viewport'));