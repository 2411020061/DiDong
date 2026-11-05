"""Generate the editable report framework. Requires python-docx (pip install python-docx)."""
from pathlib import Path
from docx import Document
from docx.shared import Cm, Pt, RGBColor
from docx.enum.section import WD_SECTION_START
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

OUT = Path(__file__).resolve().parent
D = Document()
TITLE = 'XÂY DỰNG ỨNG DỤNG ANDROID HỖ TRỢ TẬP GIÃN CƠ VÀ THEO DÕI TƯ THẾ BẰNG CAMERA'

def font(style, size=14, bold=False, color='000000'):
    style.font.name = 'Times New Roman'
    style.font.size = Pt(size)
    style.font.bold = bold
    style.font.color.rgb = RGBColor.from_string(color)
    fonts = style.element.get_or_add_rPr().rFonts
    for attr in list(fonts.attrib):
        if attr.endswith('Theme'): del fonts.attrib[attr]
    for key in ['ascii', 'hAnsi', 'eastAsia', 'cs']:
        fonts.set(qn('w:' + key), 'Times New Roman')

font(D.styles['Normal'])
D.styles['Normal'].paragraph_format.line_spacing = 1.5
D.styles['Normal'].paragraph_format.space_after = Pt(6)
for name, size in [('Heading 1', 16), ('Heading 2', 14), ('Heading 3', 14)]:
    font(D.styles[name], size, True)
    D.styles[name].paragraph_format.space_before = Pt(8)
    D.styles[name].paragraph_format.space_after = Pt(6)
    D.styles[name].paragraph_format.line_spacing = 1.15
for name, color in [('HuongDan', '666666'), ('ChuThichHinh', '000000'), ('ChuThichBang', '000000')]:
    st = D.styles.add_style(name, 1)
    font(st, 12, False, color)
    st.paragraph_format.line_spacing = 1.15
    st.paragraph_format.space_after = Pt(5)
    if name != 'HuongDan':
        st.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
        st.paragraph_format.keep_with_next = True

def setup_section(sec):
    sec.page_width, sec.page_height = Cm(21), Cm(29.7)
    sec.top_margin = sec.bottom_margin = Cm(2)
    sec.left_margin, sec.right_margin = Cm(3.5), Cm(2)
    sec.header_distance = sec.footer_distance = Cm(1)

setup_section(D.sections[0])

def p(text='', style=None):
    para = D.add_paragraph(text, style)
    if style is None:
        para.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    return para

def h(text, level=2):
    return D.add_heading(text, level)

def note(text):
    return p('[CẦN HOÀN THIỆN] ' + text, 'HuongDan')

def page():
    D.add_page_break()

def field(para, instruction, fallback='Cập nhật trường trong Microsoft Word: Ctrl+A → F9.'):
    r = OxmlElement('w:r')
    begin = OxmlElement('w:fldChar'); begin.set(qn('w:fldCharType'), 'begin')
    code = OxmlElement('w:instrText'); code.set(qn('xml:space'), 'preserve'); code.text = instruction
    sep = OxmlElement('w:fldChar'); sep.set(qn('w:fldCharType'), 'separate')
    text = OxmlElement('w:t'); text.text = fallback
    end = OxmlElement('w:fldChar'); end.set(qn('w:fldCharType'), 'end')
    for node in (begin, code, sep, text, end): r.append(node)
    para._p.append(r)

def numbered_section(fmt):
    sec = D.add_section(WD_SECTION_START.NEW_PAGE)
    setup_section(sec)
    sec.footer.is_linked_to_previous = False
    sec.header.is_linked_to_previous = False
    for previous in list(sec._sectPr.findall(qn('w:pgNumType'))):
        sec._sectPr.remove(previous)
    pg = OxmlElement('w:pgNumType'); pg.set(qn('w:fmt'), fmt); pg.set(qn('w:start'), '1')
    sec._sectPr.append(pg)
    para = sec.footer.paragraphs[0]
    para.alignment = WD_ALIGN_PARAGRAPH.CENTER
    field(para, ' PAGE ', '1')

def front(title):
    para = p(title)
    para.alignment = WD_ALIGN_PARAGRAPH.CENTER
    para.runs[0].bold = True
    para.runs[0].font.size = Pt(16)

def cover(inner=False):
    for txt in ['TRƯỜNG ĐẠI HỌC CỬU LONG', 'KHOA CÔNG NGHỆ THÔNG TIN – TRUYỀN THÔNG']:
        para = p(txt); para.alignment = WD_ALIGN_PARAGRAPH.CENTER; para.runs[0].bold = True
    p()
    para = p('[CHÈN LOGO CHÍNH THỨC CỦA TRƯỜNG]', 'HuongDan'); para.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p()
    para = p('[HỌ VÀ TÊN SINH VIÊN]'); para.alignment = WD_ALIGN_PARAGRAPH.CENTER
    if inner:
        para = p('MSSV: [ĐIỀN MSSV]'); para.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p()
    para = p(TITLE); para.alignment = WD_ALIGN_PARAGRAPH.CENTER
    para.runs[0].bold = True; para.runs[0].font.size = Pt(18)
    p()
    para = p('ĐỒ ÁN TỐT NGHIỆP NGÀNH CÔNG NGHỆ THÔNG TIN'); para.alignment = WD_ALIGN_PARAGRAPH.CENTER
    if inner:
        p()
        para = p('Giảng viên hướng dẫn\n[HỌC HÀM, HỌC VỊ, HỌ VÀ TÊN GVHD]'); para.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p()
    para = p('Vĩnh Long – [NĂM NỘP BÁO CÁO]'); para.alignment = WD_ALIGN_PARAGRAPH.CENTER
    para.paragraph_format.space_before = Pt(70 if inner else 200)

def figure(number, title, lines):
    table = D.add_table(rows=1, cols=1)
    table.style = 'Table Grid'
    cell = table.cell(0, 0)
    cell.text = '[CHÈN ' + title.upper() + ' TẠI ĐÂY]'
    for para in cell.paragraphs:
        para.alignment = WD_ALIGN_PARAGRAPH.CENTER
        for run in para.runs: run.font.size = Pt(11)
    p('Hình ' + number + '. ' + title, 'ChuThichHinh')
    # One description paragraph with five explicit lines, as requested by the teacher.
    description = p('\n'.join(lines))
    description.alignment = WD_ALIGN_PARAGRAPH.LEFT

def table(number, title, rows):
    p('Bảng ' + number + '. ' + title, 'ChuThichBang')
    t = D.add_table(rows=0, cols=len(rows[0])); t.style = 'Table Grid'
    for row in rows:
        cells = t.add_row().cells
        for cell, text in zip(cells, row):
            cell.text = text
            for para in cell.paragraphs:
                para.paragraph_format.line_spacing = 1
                para.paragraph_format.space_after = Pt(3)
                for run in para.runs: run.font.size = Pt(11)

def chapter(n, name):
    h(f'CHƯƠNG {n}: {name}', 1)

# SECTION 1: two unnumbered covers.
cover(); page(); cover(True)

# SECTION 2: Roman numerals, restarting at i.
numbered_section('lowerRoman')
front('LỜI CẢM ƠN')
p('Em xin chân thành cảm ơn quý thầy cô Trường Đại học Cửu Long, Khoa Công nghệ thông tin – Truyền thông đã giảng dạy và hỗ trợ em trong quá trình học tập. Em xin gửi lời cảm ơn đến [HỌ TÊN GVHD] đã hướng dẫn em thực hiện đề tài xây dựng ứng dụng Android hỗ trợ tập giãn cơ và theo dõi tư thế bằng camera.')
p('Em cảm ơn gia đình, bạn bè và những người đã hỗ trợ em trong quá trình tìm hiểu công nghệ và thử nghiệm ứng dụng. Do kiến thức và thời gian thực hiện còn hạn chế, báo cáo có thể còn thiếu sót. Em mong nhận được ý kiến đóng góp của quý thầy cô để tiếp tục hoàn thiện đề tài.')
note('Điều chỉnh lời cảm ơn theo sự hỗ trợ thực tế; bổ sung địa điểm, ngày tháng và chữ ký.')
page(); front('TÓM TẮT LUẬN VĂN')
p('Đề tài hướng đến xây dựng BodyPoint, ứng dụng Android hỗ trợ quan sát tư thế trong một số bài tập giãn cơ bằng camera. Luồng xử lý dự kiến gồm thu nhận khung hình bằng CameraX, nhận người và điểm khớp bằng mô hình TFLite, đánh giá điều kiện tư thế và đếm thời gian giữ đúng. Ứng dụng cung cấp danh sách bài tập và chức năng theo dõi các lượt tập được lưu trên thiết bị.')
p('Trong mã nguồn hiện tại, YOLO được dùng để nhận người và khớp; quy tắc hình học được dùng để đánh giá tư thế. Chưa đủ bằng chứng để khẳng định đã chấm đúng/sai bằng model huấn luyện riêng hoặc nhận diện mượt trên LDPlayer. Việc đánh giá trên camera thật cần được trình bày bằng dữ liệu thực nghiệm và giới hạn cụ thể.')
note('Khi hoàn tất, thay đoạn trạng thái trên bằng tóm tắt kết quả có số liệu thật. Không dùng tỷ lệ test phần mềm làm độ chính xác nhận diện tư thế.')
p('Từ khóa: Android, CameraX, TFLite, nhận diện tư thế, giãn cơ, BodyPoint.')
page(); front('DANH MỤC HÌNH ẢNH')
field(p(), ' TOC \\h \\z \\t "ChuThichHinh,1" ')
note('Sau khi chèn hình thật, giữ nguyên caption và cập nhật toàn bộ danh mục bằng Ctrl+A → F9. Số hình có dạng chương.số_thứ_tự.')
page(); front('DANH MỤC BẢNG')
field(p(), ' TOC \\h \\z \\t "ChuThichBang,1" ')
note('Bổ sung bảng kiểm thử và kết quả thực nghiệm khi có số liệu. Không điền số liệu giả.')
page(); front('DANH MỤC TỪ VIẾT TẮT')
table('0.1', 'Các từ viết tắt sử dụng trong báo cáo', [
    ['Từ viết tắt', 'Giải thích'], ['AI', 'Artificial Intelligence – Trí tuệ nhân tạo'],
    ['API', 'Application Programming Interface – Giao diện lập trình ứng dụng'],
    ['APK', 'Android Package – Gói cài đặt ứng dụng Android'], ['CSDL', 'Cơ sở dữ liệu'],
    ['FPS', 'Frames Per Second – Số khung hình mỗi giây'], ['RGB', 'Red, Green, Blue'],
    ['SDK', 'Software Development Kit – Bộ công cụ phát triển'],
    ['TFLite', 'TensorFlow Lite'], ['UI', 'User Interface – Giao diện người dùng'],
    ['UML', 'Unified Modeling Language – Ngôn ngữ mô hình hóa thống nhất'],
    ['YOLO', 'You Only Look Once – Họ mô hình nhận diện đối tượng'],
    ['YUV', 'Biểu diễn độ sáng Y và thành phần màu U, V']])
page(); front('MỤC LỤC')
field(p(), ' TOC \\o "1-3" \\h \\z \\u ')
note('Mở bằng Microsoft Word, Ctrl+A → F9 → Update entire table. Mục lục tự lấy Heading 1, Heading 2 và Heading 3.')

# SECTION 3: Arabic page numbers, restarting at 1. Five reserved pages per chapter.
numbered_section('decimal')
chapter(1, 'GIỚI THIỆU')
h('1.1 Giới thiệu đề tài')
p('Việc dùng camera trên thiết bị di động tạo điều kiện quan sát động tác và cung cấp phản hồi trực quan trong lúc tập. Trong phạm vi đề tài, ứng dụng BodyPoint được xây dựng để thử nghiệm luồng nhận người, xác định các điểm khớp và theo dõi thời gian giữ tư thế khi thực hiện một số bài giãn cơ. Người dùng cần nhìn thấy điểm khớp thay đổi theo chuyển động, thay vì chỉ nhìn ảnh camera hoặc ảnh minh họa tĩnh.')
note('Triển khai bối cảnh, lý do chọn đề tài, đối tượng sử dụng và bài toán thực tế. Bổ sung nguồn tham khảo cho các nhận định về vận động hoặc sức khỏe; không tự đưa số liệu y khoa.')
h('1.1.1 Bài toán cần giải quyết', 3)
p('Ứng dụng phải phân biệt việc camera đang phát hình với việc mô hình đã nhận được người. Phản hồi đúng tư thế chỉ có ý nghĩa khi các khớp cần thiết đủ rõ và nằm trong vùng hiển thị. Bộ đếm phải phản ánh thời gian quan sát tư thế hợp lệ liên tục, không được cộng thời gian khi mất người hoặc camera ngừng trả kết quả.')
note('Trang 1/5 của Chương 1: hoàn thiện nội dung giới thiệu trước khi chuyển trang.')
page(); h('1.2 Mục tiêu đề tài')
p('Mục tiêu tổng quát là xây dựng và đánh giá một nguyên mẫu Android hỗ trợ quan sát tư thế tập giãn cơ bằng camera, cung cấp phản hồi trực quan và lưu lịch sử tập trên thiết bị.')
h('1.2.1 Mục tiêu cụ thể', 3)
for text in ['Thu nhận và xử lý các khung hình CameraX liên tiếp trên luồng phân tích riêng.', 'Nhận người và điểm khớp bằng YOLO TFLite; vẽ đúng vị trí sau xoay, cắt, đệm và phản chiếu camera trước.', 'Đánh giá tư thế theo quy tắc hiện có; tách rõ khả năng nhận khớp và khả năng phân loại đúng/sai.', 'Đếm thời gian giữ đúng liên tục, dừng và đặt lại khi dữ liệu không hợp lệ.', 'Lưu lượt tập, thời gian và chỉ số quan sát; hiển thị phần Theo dõi.']:
    p('• ' + text)
note('Bổ sung tiêu chí nghiệm thu đo được: thiết bị thử, tốc độ suy luận, tỷ lệ mất khớp và cách định nghĩa tư thế đúng. Chưa đặt các con số mục tiêu như kết quả đã đạt.')
page(); h('1.3 Nội dung đề tài')
p('Nội dung thực hiện gồm khảo sát công nghệ, phân tích yêu cầu, thiết kế luồng sử dụng, hiện thực ứng dụng và kiểm thử. Phần xử lý ảnh cần kiểm soát định dạng dữ liệu, tỉ lệ ảnh đầu vào và phép biến đổi tọa độ. Phần đánh giá tư thế cần mô tả rõ các quy tắc hoặc model thực sự được sử dụng.')
table('1.1', 'Kế hoạch triển khai nội dung đề tài', [['Công việc', 'Minh chứng cần bổ sung'], ['Khảo sát', 'Nguồn chính thức và bảng so sánh'], ['Thiết kế', 'Các sơ đồ UML và luồng màn hình'], ['Hiện thực', 'Mã nguồn, ảnh giao diện, APK'], ['Kiểm thử', 'Test tự động và biên bản thử camera thật']])
note('Mỗi công việc cần mô tả cách làm, đầu vào, đầu ra và tiêu chí hoàn tất. Bổ sung tiến độ thực tế; không tự suy diễn ngày tháng thực hiện.')
page(); h('1.4 Giới hạn đề tài')
p('Đề tài tập trung vào ứng dụng Android và tình huống một người tập trong vùng camera. Luồng hiện tại dùng mô hình nhận khớp YOLO; chưa có kết quả thực nghiệm đủ để bảo đảm chất lượng trên mọi loại webcam hoặc máy ảo. Các khớp bàn tay chi tiết không thuộc bộ 17 điểm COCO nên việc chấm động tác cổ tay phải nêu rõ giới hạn.')
p('Nguồn dữ liệu, điều kiện ánh sáng, khoảng cách camera và khả năng xử lý của thiết bị đều ảnh hưởng kết quả. Ảnh vẽ được khung người trong Colab chỉ chứng minh nhận diện trên ảnh đó; không chứng minh tốc độ và độ chính xác của chuỗi video CameraX.')
note('Phân biệt phần có trong mã nguồn với phần đã chạy thử. Ghi rõ danh sách bài đang mở trên giao diện, phần model chưa tích hợp và điều kiện thử nghiệm chưa thực hiện.')
page(); h('1.5 Cấu trúc luận văn')
for text in ['Chương 1 trình bày bối cảnh, mục tiêu, nội dung và giới hạn đề tài.', 'Chương 2 trình bày cơ sở lý thuyết và các nghiên cứu liên quan.', 'Chương 3 trình bày yêu cầu, phân tích và thiết kế hệ thống.', 'Chương 4 trình bày cài đặt, hiện thực, lưu dữ liệu và kiểm thử.', 'Chương 5 tổng kết kết quả, hạn chế, đóng góp và hướng phát triển.']:
    p(text)
note('Tổng hợp phạm vi đã xác định và giải thích mối liên hệ giữa các chương. Chương 1 có 5 trang dự trù; cần viết đầy đủ nội dung thay vì giữ khoảng trống để đủ trang.')

page(); chapter(2, 'CƠ SỞ LÝ THUYẾT VÀ CÁC NGHIÊN CỨU LIÊN QUAN')
h('2.1 Cơ sở lý thuyết')
h('2.1.1 Android và CameraX', 3)
p('CameraX cung cấp các use case như Preview và ImageAnalysis. Trong luồng hiện tại, Preview hiển thị camera, còn ImageAnalysis đưa các khung hình đến executor xử lý riêng. Chiến lược KEEP_ONLY_LATEST giúp bỏ khung hình chờ cũ khi suy luận chậm, tránh tích lũy hàng đợi video.')
note('Trình bày vòng đời camera, quyền truy cập, PreviewView, ImageProxy, cơ chế đóng frame và lý do tách luồng giao diện khỏi suy luận. Dẫn nguồn tài liệu CameraX chính thức.')
page(); h('2.1.2 Biểu diễn ảnh và biến đổi tọa độ', 3)
p('Khung hình YUV_420_888 gồm ba plane có thể có row stride và pixel stride khác nhau. Sau chuyển sang RGB, ảnh cần được xoay theo metadata CameraX. Trước khi đưa vào YOLO, giữ tỉ lệ ảnh và đệm thành kích thước đầu vào; sau suy luận phải bỏ đệm rồi ánh xạ các điểm qua vùng crop và phép phản chiếu camera trước.')
figure('2.1', 'Sơ đồ xử lý ảnh và ánh xạ tọa độ', ['Sơ đồ bắt đầu từ ba plane YUV của camera.', 'Ảnh được đổi màu và xoay theo metadata.', 'Letterbox giữ tỉ lệ trước khi chạy mô hình.', 'Tọa độ đầu ra được bỏ đệm và ánh xạ crop.', 'Điểm khớp được vẽ lên đúng vùng preview.'])
note('Bổ sung công thức scale, padding, đổi tọa độ và ví dụ kiểm tra bằng ảnh dọc/ngang.')
page(); h('2.1.3 Nhận diện người và ước lượng tư thế', 3)
p('Mô hình hiện có trong assets là yolo26m-pose.tflite, nhận người và 17 khớp COCO. Đầu vào được kiểm tra là float32 [1,3,640,640], đầu ra [1,56,8400]. Graph đã chuẩn hóa tọa độ và tính xác suất; không được chia tọa độ thêm cho 640 hoặc áp sigmoid lần nữa.')
p('Nhận khớp và chấm đúng/sai là hai bài toán khác nhau. Quy tắc hình học dùng vị trí khớp để kiểm tra điều kiện tư thế. Model phân loại riêng cần dữ liệu đầu vào, cách chuẩn hóa và nhãn phù hợp với dữ liệu dùng khi huấn luyện; việc đổi 17 điểm COCO thành 33 chỉ số không tạo ra 33 điểm MediaPipe 3D thật.')
note('Bổ sung mô tả 17 khớp, ngưỡng tin cậy, lựa chọn người và giới hạn che khuất. Nếu đưa kết quả Colab vào báo cáo, ghi đúng notebook, model và cách xuất thực tế.')
page(); h('2.1.4 Đánh giá tư thế và thời gian giữ đúng', 3)
p('NeckPoseEvaluator và ExercisePoseEvaluator kiểm tra các điều kiện tư thế trong mã hiện tại. PostureHoldTimer cộng thời gian giữa các quan sát hợp lệ liên tiếp, giới hạn khoảng cách khung hình và đặt lại khi tư thế không hợp lệ. Mốc hoàn tất trong ứng dụng là 15 giây.')
table('2.1', 'Phân biệt các chỉ số', [['Chỉ số', 'Ý nghĩa'], ['Độ tin cậy person', 'Điểm nhận người từ detector'], ['Tỷ lệ quan sát đúng', 'Tỷ lệ kết quả hợp lệ theo quy tắc ứng dụng'], ['Độ chính xác phân loại', 'Cần bộ dữ liệu có nhãn và đánh giá riêng'], ['FPS / độ trễ', 'Khả năng xử lý khung hình trên thiết bị thử']])
note('Không dùng giá trị person 0,88 hoặc tỷ lệ test pass để khẳng định động tác đúng 88%. Bổ sung công thức và nguồn khi mô tả thuật toán.')
page(); h('2.2 Các nghiên cứu liên quan')
h('2.2.1 Nghiên cứu trong nước', 3)
note('Chọn ít nhất một công trình phù hợp, ghi tác giả, năm, bài toán, phương pháp, dữ liệu, kết quả và hạn chế. Chỉ đưa công trình đã đọc và kiểm tra được nguồn.')
h('2.2.2 Nghiên cứu ngoài nước', 3)
note('Khảo sát ước lượng tư thế và phản hồi bài tập qua camera; đối chiếu với yêu cầu BodyPoint. Tài liệu công nghệ chính thức không tự thay thế cho công trình nghiên cứu.')
h('2.2.3 So sánh và lựa chọn hướng giải quyết', 3)
table('2.2', 'Khung so sánh nghiên cứu liên quan', [['Công trình / nguồn', 'Phương pháp', 'Hạn chế / liên hệ'], ['[Nguồn trong nước]', '[Điền sau khi đọc]', '[Điền]'], ['[Nguồn ngoài nước]', '[Điền sau khi đọc]', '[Điền]']])

page(); chapter(3, 'PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG')
h('3.1 Yêu cầu hệ thống')
p('Hệ thống cần hiển thị bài tập, mở camera sau khi được cấp quyền, nhận khớp liên tục và cung cấp phản hồi. Khi giữ đúng đủ thời gian, hệ thống lưu lượt tập; khi thiếu khớp, mất người hoặc mất kết quả mới, bộ đếm không được tiếp tục cộng.')
h('3.2 Phân tích hệ thống')
h('3.2.1 Thu thập yêu cầu người dùng', 3)
note('Mô tả phương pháp thu thập đã thực hiện. Nếu mới dựa trên yêu cầu đề tài thì ghi rõ, không viết như đã khảo sát nhiều người.')
h('3.2.2 Phân tích yêu cầu người dùng', 3)
note('Phân loại yêu cầu chức năng và phi chức năng: độ trễ, khả năng nhìn rõ phản hồi, xử lý mất camera và lưu dữ liệu cục bộ.')
h('3.2.3 Yêu cầu phải đạt được khi thiết kế/hiện thực phần mềm', 3)
note('Lập tiêu chí cho từng yêu cầu, cách đo và tình huống nghiệm thu. Gắn mỗi yêu cầu với ca kiểm thử ở Chương 4.')
page(); h('3.3 Thiết kế phần mềm')
h('3.3.1 Thiết kế CSDL', 3)
p('Luồng lưu lượt tập hiện tại dùng SharedPreferences qua ExerciseSessionStore. Cần mô tả cấu trúc khóa theo ngày, loại bài, thời gian và tổng số quan sát đúng. Các lớp Room cũ còn trong repository chưa đủ chứng minh luồng bài tập hiện dùng Room; không mô tả một CSDL SQL chưa triển khai như kết quả hiện thực.')
table('3.1', 'Cấu trúc lưu dữ liệu lượt tập hiện tại', [['Dữ liệu', 'Dạng khóa / giá trị'], ['Tổng lượt theo ngày', 'yyyy-MM-dd → int'], ['Lượt theo loại bài', 'date:type → int'], ['Giờ tập', 'date:time:index → HH:mm'], ['Loại bài của lượt', 'date:typeindex:index → type'], ['Tổng và số lần đo', 'accuracy_sum / accuracy_count + date:type']])
h('3.3.2 Thiết kế các usecase', 3)
note('Vẽ các use case: chọn bài, cấp quyền, mở camera, theo dõi tư thế, hoàn tất và xem lịch sử. Đặc tả tác nhân, điều kiện trước/sau và luồng ngoại lệ.')
h('3.3.3 Lược đồ triển khai', 3)
note('Vẽ Android/LDPlayer, webcam, APK và model cục bộ. Nếu dùng cầu nối Windows thì thể hiện riêng kết nối debug, không coi đó là đường CameraX mặc định.')
page(); h('3.3.4 Lược đồ trình tự', 3)
figure('3.1', 'Lược đồ trình tự nhận diện và đếm thời gian', ['Người dùng chọn bài và cấp quyền camera.', 'CameraX chuyển frame sang executor phân tích.', 'Detector trả khớp để đánh giá tư thế.', 'Kết quả được gửi về luồng giao diện.', 'Timer và lưu lịch sử nhận cùng kết quả đó.'])
h('3.3.5 Lược đồ cộng tác', 3)
note('Vẽ các đối tượng Fragment, Detector, Evaluator, Timer, Overlay và SessionStore; đánh số thông điệp trao đổi và giải thích ít nhất 5 dòng dưới hình khi chèn.')
h('3.3.6 Lược đồ trạng thái', 3)
figure('3.2', 'Lược đồ trạng thái phiên tập', ['Phiên tập bắt đầu ở trạng thái chờ camera.', 'Không có người thì chưa cộng thời gian.', 'Đúng tư thế chuyển sang trạng thái giữ đúng.', 'Mất khớp hoặc sai làm đặt lại thời gian.', 'Đủ 15 giây chuyển sang lưu và hoàn tất.'])
page(); h('3.3.7 Lược đồ lớp', 3)
figure('3.3', 'Lược đồ lớp xử lý bài tập', ['ExerciseFragment điều phối phiên tập.', 'YoloPoseDetector đọc model và nhận khớp.', 'Evaluator kiểm tra điều kiện tư thế.', 'Overlay và Timer hiển thị kết quả.', 'SessionStore lưu dữ liệu sau khi hoàn tất.'])
h('3.3.8 Lược đồ hoạt động', 3)
note('Vẽ vòng lặp lấy frame → xử lý → có người? → đủ khớp? → đúng? → đủ thời gian?; bổ sung nhánh mất quyền, lỗi model và thoát màn hình.')
h('3.4 Thiết kế giao diện')
note('Trình bày màn Trang chủ, danh sách bài, camera và Theo dõi. Đối chiếu màu khung, nội dung phản hồi và đồng hồ giữa trạng thái đúng/sai/mất người.')
page(); h('3.5 Yêu cầu phần cứng, phần mềm')
table('3.2', 'Môi trường xây dựng và môi trường thử nghiệm', [['Thành phần', 'Thông tin'], ['Ứng dụng', 'Android, minSdk 27, target/compile SDK 37'], ['Build hiện tại', 'Java 25, Gradle 9.7.1, AGP 9.4.0'], ['Công cụ phát triển', 'Android Studio [điền phiên bản trên máy bạn]'], ['Máy ảo', 'LDPlayer 14 [điền cấu hình CPU/RAM/camera]'], ['Thiết bị thật', '[Điền nếu đã thử]']])
h('3.6 Liên kết giao diện')
figure('3.4', 'Sơ đồ liên kết giao diện ứng dụng', ['Trang chủ cung cấp đường vào danh sách bài.', 'Chọn bài sẽ mở màn hình camera tương ứng.', 'Người dùng theo dõi khớp và thời gian giữ.', 'Hoàn tất chuyển sang màn hình Theo dõi.', 'Mỗi đường thoát cần giải phóng tài nguyên.'])
note('Đối chiếu các sơ đồ trong Chương 3 với code hiện tại; phân biệt thiết kế dự kiến và chức năng đã có.')

page(); chapter(4, 'HIỆN THỰC HỆ THỐNG')
h('4.1 Hướng dẫn cài đặt')
p('Lấy mã nguồn từ repository GitHub, mở bằng Android Studio và đồng bộ Gradle. Chọn thiết bị Android hoặc LDPlayer đã kết nối ADB rồi chạy bản debug. Khi thử bằng APK, cần cài bản được build từ cùng phiên bản mã nguồn để tránh so sánh nhầm bản cũ và bản mới.')
note('Bổ sung hướng dẫn có ảnh theo máy thực: phiên bản Android Studio, đường dẫn SDK, kết nối LDPlayer, cấp quyền camera và cách kiểm tra ứng dụng đang chạy đúng bản.')
p('Bản cloud đã build được APK debug. Kết quả này xác minh khả năng biên dịch và đóng gói, chưa chứng minh khả năng nhận khớp liên tục trên webcam LDPlayer của người dùng.')
page(); h('4.2 Hiện thực giao diện')
figure('4.1', 'Màn hình camera và các trạng thái phản hồi', ['Ảnh cần lấy từ bản APK đang được thử.', 'Khung người phải trùng người trong preview.', 'Các điểm khớp cập nhật theo chuyển động.', 'Màu và chữ phân biệt đúng với chưa đúng.', 'Đồng hồ chỉ ghi nhận khi đủ điều kiện.'])
note('Chèn thêm ảnh Trang chủ, danh sách bài và Theo dõi nếu cần. Mỗi ảnh phải có caption, một đoạn mô tả ít nhất 5 dòng và liên hệ chức năng cụ thể.')
note('Không dùng ảnh suy luận tĩnh trong Colab thay cho ảnh chứng minh nhận diện video liên tục trên CameraX.')
page(); h('4.3 Hiện thực CSDL')
p('ExerciseSessionStore ghi lượt tập bằng SharedPreferences khi hoàn tất. Thông tin gồm ngày, loại bài, giờ tập và các tổng phục vụ chỉ số quan sát đúng. Đây là cách lưu khóa–giá trị cục bộ; cần mô tả đúng giới hạn về truy vấn và quản lý lịch sử so với CSDL quan hệ.')
figure('4.2', 'Ảnh chụp code lưu một lượt tập', ['Code được trích từ ExerciseSessionStore.', 'Khóa ngày xác định nhóm lượt tập cần lưu.', 'Loại bài và thời điểm được ghi theo lượt.', 'Tổng và số lần đo phục vụ tính chỉ số.', 'Hàm chỉ được gọi sau khi giữ đủ 15 giây.'])
note('Trình bày cả cách đọc lại và kiểm tra dữ liệu. Không đưa thông tin cá nhân thật vào ảnh.')
page(); h('4.4 Hiện thực các báo cáo (report)')
p('Trong phạm vi ứng dụng, phần report được trình bày bằng màn Theo dõi và dữ liệu lượt tập đã lưu. Không khẳng định có chức năng xuất PDF, Excel hoặc báo cáo máy chủ nếu chưa hiện thực. Cần phân biệt chỉ số quan sát đúng trong phiên với độ chính xác của một model được đánh giá trên bộ dữ liệu có nhãn.')
figure('4.3', 'Màn hình Theo dõi và dữ liệu lượt tập', ['Ảnh mô tả kết quả được lưu sau phiên tập.', 'Ngày tập liên kết với các lượt đã hoàn tất.', 'Loại bài và giờ tập cần khớp dữ liệu gốc.', 'Chỉ số đúng được giải thích theo cách tính.', 'Ảnh minh họa phải ghi rõ nếu dùng dữ liệu mẫu.'])
note('Bổ sung trường hợp chưa có dữ liệu, nhiều lượt trong ngày và kết quả khi mở lại ứng dụng.')
page(); h('4.5 Hiện thực code liên quan')
figure('4.4', 'Ảnh chụp code đọc khớp hoặc bộ đếm', ['Chỉ chọn một đoạn code tiêu biểu, dễ đọc.', 'Nêu tên lớp và mục đích xử lý của đoạn đó.', 'Giải thích đầu vào và kết quả được trả về.', 'Làm rõ điều kiện sai và nhánh xử lý lỗi.', 'Liên hệ đoạn code với hành vi trên camera.'])
table('4.1', 'Kết quả kiểm tra mã nguồn tại thời điểm dựng khung', [['Kiểm tra', 'Kết quả'], ['assembleDebug', 'Đóng gói APK thành công'], ['Unit/Robolectric', '28 test: 24 đạt, 2 lỗi, 2 bỏ qua'], ['Hai test lỗi', 'Thiếu stretch_classifier.tflite'], ['Hai test bỏ qua', 'Host thiếu JNI Android TensorFlow Lite'], ['LDPlayer / webcam thật', 'Chưa xác minh lại sau sửa đổi']])
note('Bổ sung bảng thử video: cấu hình máy, thời lượng, ánh sáng, khoảng cách, độ trễ, mất người, mất camera. Ghi ngày và phiên bản code; không xem test decoder là kiểm chứng suy luận model trên camera thật.')

page(); chapter(5, 'KẾT LUẬN')
h('5.1 Kết quả đạt được')
p('Mã nguồn đã có luồng CameraX, xử lý khung hình, detector TFLite, vẽ khớp, kiểm tra tư thế và lưu phiên tập. Các sửa đổi bao gồm đọc đúng đầu ra chuẩn hóa của model, giữ tỉ lệ ảnh bằng letterbox, xử lý stride YUV và hiển thị trạng thái tư thế với đồng hồ. Khả năng build APK và các test phần mềm đã được kiểm tra trong cloud.')
note('Tổng hợp lại theo từng mục tiêu ở Chương 1. Chỉ ghi nhận diện liên tục trên LDPlayer là kết quả đạt được sau khi chạy thử và có bằng chứng; hiện vấn đề này cần tiếp tục xác minh trên máy người dùng.')
page(); h('5.2 Ưu và nhược điểm')
h('5.2.1 Ưu điểm', 3)
p('Kiến trúc tách các thành phần nhận khớp, đánh giá tư thế, hiển thị và lưu dữ liệu giúp xác định lỗi từng phần. Phân tích khung hình trên executor riêng và bỏ frame chờ cũ giúp tránh tạo hàng đợi video không giới hạn.')
h('5.2.2 Nhược điểm', 3)
p('Model pose có kích thước khoảng 83 MB nên tốc độ trên máy ảo cần được đo. Bộ khớp COCO không có điểm ngón tay chi tiết. Mô hình phân loại từ Colab chưa được xác minh tương thích và chưa có trong assets của checkout này. Hai test liên quan model phân loại vẫn lỗi vì thiếu asset.')
note('Bổ sung hạn chế dựa trên số liệu và quan sát thực; không suy luận hiệu quả sức khỏe từ việc nhận được khớp.')
page(); h('5.3 Đóng góp của đồ án')
p('Đóng góp dự kiến của đồ án là xây dựng nguyên mẫu Android kết nối các bước thu nhận ảnh, nhận khớp, phản hồi tư thế và lưu phiên tập trong cùng một luồng sử dụng. Các kiểm tra tọa độ và stride giúp mô tả rõ những điều kiện kỹ thuật cần đúng trước khi đánh giá chất lượng nhận diện.')
note('Nêu đúng phần sinh viên tự thực hiện, phần dùng thư viện và phần kế thừa. Không nhận thuật toán YOLO hoặc CameraX là thuật toán do đồ án tự đề xuất.')
note('Bổ sung đóng góp có thể kiểm chứng: mã nguồn, sơ đồ, test, dữ liệu tự thu thập hoặc phương pháp đánh giá nếu thực sự đã làm.')
page(); h('5.4 Hướng mở rộng tương lai')
p('Các hướng phát triển gồm đo và tối ưu độ trễ trên thiết bị thật; thử model nhẹ hơn khi cần; bổ sung dữ liệu có nhãn từ nhiều người; tích hợp model đúng/sai với tiền xử lý tương thích; và kiểm tra bàn tay bằng bộ landmark phù hợp trước khi chấm cổ tay.')
table('5.1', 'Khung lập kế hoạch mở rộng', [['Hạng mục', 'Điều kiện đánh giá hoàn tất'], ['Nhận khớp liên tục', 'Đo độ trễ và mất khớp trên video thật'], ['Model đúng/sai', 'Có model, nhãn, tiền xử lý và tập test độc lập'], ['Theo dõi dài hạn', 'Kiểm tra cấu trúc lưu và truy vấn dữ liệu'], ['Trải nghiệm camera', 'Xử lý mất quyền, ngắt camera, thoát màn hình']])
note('Xếp thứ tự ưu tiên, nguồn lực và phạm vi. Mỗi đề xuất phải gắn với một hạn chế đã nêu, không viết thành chức năng đã hoàn thành.')
page(); h('5.4.1 Kế hoạch kiểm chứng hướng phát triển', 3)
p('Khi tích hợp model mới, cần kiểm tra tensor, nhãn và cách chuẩn hóa trước khi nối kết quả vào bộ đếm. Khi tối ưu tốc độ, cần so sánh trên cùng thiết bị và cùng điều kiện video. Khi bổ sung dữ liệu, cần tách các mẫu kiểm tra độc lập với mẫu huấn luyện để tránh đánh giá lạc quan do trùng người hoặc trùng ảnh.')
note('Hoàn thiện kế hoạch thử nghiệm và tiêu chí đo cụ thể. Trang này thuộc mục 5.4, dành cho nội dung thực chất; không dùng lặp lại kết luận hoặc tăng khoảng cách dòng để đủ 5 trang.')

page(); h('TÀI LIỆU THAM KHẢO', 1)
p('Danh sách dưới đây là nguồn đề xuất cần đọc và kiểm tra lại trước khi nộp. Điền tác giả, tiêu đề, năm và ngày truy cập thực tế; dùng số [1], [2]... để trích dẫn thống nhất trong nội dung.')
for txt in ['[1] Android Developers, CameraX overview. https://developer.android.com/media/camera/camerax — ngày truy cập: [điền].', '[2] Android Developers, Image analysis. https://developer.android.com/media/camera/camerax/analyze — ngày truy cập: [điền].', '[3] Ultralytics, Pose estimation documentation. https://docs.ultralytics.com/tasks/pose/ — ngày truy cập: [điền].', '[4] Google AI Edge, LiteRT documentation. https://ai.google.dev/edge/litert — ngày truy cập: [điền].', '[5] [Tác giả], [Tên công trình trong nước], [Tạp chí/hội nghị], [Năm], [Trang].', '[6] [Tác giả], [Tên công trình ngoài nước], [Tạp chí/hội nghị], [Năm], [Trang].']:
    p(txt)
note('Mẫu sách: Tác giả, tên sách, nhà xuất bản, năm. Mẫu bài báo: Tác giả, tên bài, tên tạp chí, số, năm, trang. Chỉ giữ tài liệu đã sử dụng.')

settings = D.settings.element
update = OxmlElement('w:updateFields'); update.set(qn('w:val'), 'true'); settings.append(update)
D.core_properties.title = 'Khung báo cáo đồ án BodyPoint'
D.core_properties.subject = 'Khung theo PDF hướng dẫn và ba section do giảng viên yêu cầu'
D.core_properties.author = '[Họ tên sinh viên]'
D.save(OUT / 'Khung_Bao_Cao_BodyPoint.docx')
print(OUT / 'Khung_Bao_Cao_BodyPoint.docx')
