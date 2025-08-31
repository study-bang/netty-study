1. 백로그(Backlog) 큐 크기  
백로그 큐는 TCP 3-way 핸드셰이크가 완료되었지만 아직 accept() 메서드가 호출되지 않은 연결들을 보관하는 대기열입니다. 클라이언트가 서버에 연결을 요청할 때, 서버는 이 요청을 백로그 큐에 넣어 순차적으로 처리합니다.  

설정: ServerBootstrap에 option(ChannelOption.SO_BACKLOG, int)를 설정하여 이 큐의 최대 크기를 제어합니다.  

역할: 이 큐의 크기를 조절함으로써 서버가 동시에 처리할 수 있는 동시 연결 요청의 상한선을 설정할 수 있습니다. 예를 들어, SO_BACKLOG 값을 128로 설정하면, 128개 이상의 연결 요청이 한꺼번에 들어올 경우 새로운 요청은 거부됩니다.  

주의사항: SO_BACKLOG는 커널(Kernel) 수준의 설정입니다. 운영체제(OS)마다 기본값이 다를 수 있으며, 너무 큰 값을 설정한다고 해서 항상 성능이 좋아지는 것은 아닙니다.  

2. 리스너(Listener) 수  
Netty에서 리스너는 새로운 연결 요청을 수락하는 스레드를 의미합니다. 이는 bossGroup의 EventLoop 수에 의해 결정됩니다.  

설정: EventLoopGroup bossGroup = new NioEventLoopGroup(N);와 같이 생성자 인자로 **스레드 수(N)**를 지정합니다.  

역할:  

bossGroup의 EventLoop: 새로운 TCP 연결 요청을 받아들입니다. bossGroup의 EventLoop가 여러 개이면 여러 개의 포트에서 연결을 수락하거나, 하나의 포트에서 여러 스레드가 동시에 연결 요청을 처리할 수 있습니다.  

workerGroup의 EventLoop: bossGroup이 수락한 연결들의 I/O(읽기/쓰기) 작업을 처리합니다.  

최적화: 일반적으로 bossGroup은 연결 수락만 담당하므로, **하나의 EventLoop(NioEventLoopGroup(1))**로도 충분합니다. workerGroup의 스레드 수를 CPU 코어 수에 맞게 설정하는 것이 더 일반적인 최적화 방법입니다.  

3. 자체 연결 관리 (Manual Connection Management)  
Netty의 기본 설정만으로는 연결 총 개수를 직접 제한할 수 없습니다. 따라서 애플리케이션 레벨에서 활성화된 연결을 직접 관리하여 제어해야 합니다.  

ChannelGroup: Netty는 ChannelGroup이라는 편리한 컬렉션을 제공합니다. 이 그룹은 활성화된 모든 Channel 객체를 저장하고 관리하는 데 사용됩니다. ChannelGroup은 스레드 안전하게 설계되어 있어, 여러 핸들러나 스레드에서 동시에 접근해도 안전합니다.  

구현 로직:  

ChannelActive 이벤트에서 ChannelGroup에 새로운 채널을 추가합니다.  

이때 ChannelGroup.size()를 확인하여 사전에 정의한 최대 연결 개수에 도달했는지 검사합니다.  

만약 초과했다면 ctx.close()를 호출하여 새로운 연결을 즉시 거부하고, 그렇지 않으면 연결을 허용합니다.  

ChannelInactive 이벤트에서 해당 채널을 그룹에서 제거하여 연결 상태를 최신으로 유지합니다.  

장점: 이 방법은 가장 유연하고 정확한 연결 제어 방법을 제공합니다. 서비스의 로드나 정책에 따라 동적으로 연결을 관리할 수 있습니다.  

TCP 연결 개수 제어 전체 코드  
TCP 연결 개수를 제어하는 세 가지 방법을 통합한 전체 코드입니다. ChannelGroup을 사용하여 연결 수를 직접 관리하는 것이 가장 효과적인 방법이므로, 이 로직을 중심으로 서버와 클라이언트 코드를 구성했습니다.  

전체 코드는 세 개의 파일로 나뉩니다.  

ConnectionLimitServer.java: Netty 서버를 시작하는 메인 클래스입니다.  

ConnectionLimitHandler.java: 연결 수를 직접 제어하는 핵심 로직이 담긴 핸들러입니다.  

ConnectionLimitClient.java: 서버의 연결 제한 기능을 테스트하기 위한 클라이언트입니다.  