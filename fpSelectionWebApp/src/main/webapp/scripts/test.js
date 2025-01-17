/*function testJetty() {
 $.ajax({
 url: "MainServlet",
 type: "POST",
 data: {
 "rank": document.getElementById("rank").value
 },
 success: function (response) {
 result = JSON.parse(response);
 alert("New rank is  (Rank + 1)::" + result.rank);
 },
 error: function () {
 alert("ERROR!!");
 }
 });
 }*/

var laseField, filterField;

$(document).on('click', '.laser-btn', function () {
    $(".lasers").append("<br>" + laseField);
});

$(document).on('click', '.filter-btn', function () {
    $(this).parent().parent().append(filterField);
    $(this).remove();
});

$(document).ready(function () {
    $('.tooltipNav').tooltipster({theme: 'tooltipster-shadow'});
    $('.rightDiv, .semiExhaustive').tooltipster({
        theme: 'tooltipster-shadow',
        side: 'right',
        maxWidth: 400
    });
    $('#cytometer').filestyle({
        buttonName: 'btn-info',
        buttonText: 'Browse',
        size: 'sm',
        placeholder: 'cytometer.csv'
    });
    $('.myFPInput').filestyle({
        buttonName: 'btn-info',
        buttonText: 'Browse',
        size: 'sm',
        placeholder: 'myfp_spectra.csv'
    });
    $('#FPMasterList').filestyle({
        buttonName: 'btn-info',
        buttonText: 'Browse',
        size: 'sm',
        placeholder: 'fpSpectra.csv'
    });
    $('#FPBrightness').filestyle({
        buttonName: 'btn-info',
        buttonText: 'Browse',
        size: 'sm',
        placeholder: 'fpBrightness.csv'
    });
    
    
    $("#algo").change(function () {
        $("#topPercent").toggle($(this).val() == "SomewhatExhaustiveServlet");
    });

    //Grab html for laser form
    laseField = $(".lasers").html();
    //Grab html for filter form
    filterField = $(".filters").html();

    $("#form").submit(function (event) {
        event.preventDefault();
        var url = $(this).attr('action');
        var formData = new FormData($('form')[0]);
        $.ajax({
            type: "POST",
            url: url,
            data: formData,
            cache: false,
            processData: false,
            contentType: false,
            success: function (response) {
                result = JSON.parse(response);
                document.getElementById("img").src = result.img;
                $("#p").append(result.info);
            }
        });
    });
    $("#cytometerForm").submit(function (event) {

        var arrayJSON = [];
        var laserTemp;

        $(".laserInlineForm").each(function (index, element) {
            laserTemp = $(element).serializeArray();
            arrayJSON.push(laserTemp);
        });

        console.log(arrayJSON);
        event.preventDefault();

        var url = "CustomCytoServlet";

        $.ajax({
            url: url,
            type: "POST",
            data: JSON.stringify(arrayJSON),
            success: function (response)
            {
                $(".responseDiv").text("cytometer.csv created");
                var URL = window.URL.createObjectURL(new Blob([response], {type: "text/csv;charset=utf-8;"}));
                var filename = "cytometer.csv";

                var a = document.createElement("a");
                a.href = URL;
                a.download = filename;
                document.body.appendChild(a);
                a.click();
            }
        });
    });

    $("#exhaustiveForm, #somewhatExhaustiveForm, #hillClimbingForm, #simulatedAnnealingForm").submit(function (event) {
        event.preventDefault();

        var form = document.getElementById("cytometerForm");
        var url = "customCytometer";
        var formData = new FormData(form);

        $.ajax({
            url: url,
            type: "POST",
            data: formData,
            processData: false,
            contentType: false,
            success: function (response)
            {
                $(".responseDiv").text(response);
            }
        });
    });

    $("#MainForm").submit(function (event) {
        event.preventDefault();
        var url = $("#algo").val()
        var formData = new FormData(this);
        $("#title").text("Loading...");
        $("#SNR").text("");
        $("#placeholder").hide();
        $("#img").hide();
        $("#download").hide();
        $("#downloadList").hide();
        var start = performance.now();

        $.ajax({
            url: url,
            type: "POST",
            data: formData,
            processData: false,
            contentType: false,
            success: function (response)
            {
                var end = performance.now();

                result = JSON.parse(response);
                if(result.img != null) {
                    document.getElementById("img").src = result.img;
                    $("#img").show();
                }
                
                $("#SNR").text(result.SNR);
                $("#title").text("Time taken was: " + (end - start) / 1000 + " s");
                $("#download").attr("href", result.img);
                $("#downloadList").attr('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent($("#SNR").text()));
                $("#download").show();
                $("#downloadList").show();
            }
        });
    });
});
